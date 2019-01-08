package io.gridgo.connector.scheduler;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.joo.promise4j.impl.AsyncDeferredObject;

import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.ConnectionRef;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchedulerConsumer extends AbstractConsumer {

    private static final Map<String, ScheduledExecutorService> executors = new NonBlockingHashMap<>();

    private static final Map<String, ConnectionRef<String>> connRefs = new NonBlockingHashMap<>();

    private String schedulerName;

    private Integer threads;

    private Long delay;

    private Long period;

    private Boolean fixedRate;

    private Boolean fixedDelay;

    private Integer errorThreshold;

    private Integer idleThreshold;

    private Integer backoffMultiplier;

    private ScheduledFuture<?> future;

    private Supplier<Message> generator = this::createDefaultMessage;

    private AtomicInteger errorCounter = new AtomicInteger();

    private AtomicInteger idleCounter = new AtomicInteger();

    private AtomicInteger backoffCounter = new AtomicInteger();

    private boolean daemon;

    public SchedulerConsumer(ConnectorContext context, String name, BObject params) {
        super(context);
        this.schedulerName = name;
        this.threads = params.getInteger("threads", 1);
        this.delay = params.getLong("delay", 1000);
        this.period = params.getLong("period", 1000);
        this.fixedRate = params.getBoolean("fixedRate", false);
        this.fixedDelay = params.getBoolean("fixedDelay", false);
        var generator = params.getString("generator");
        if (generator != null)
            this.generator = context.getRegistry().lookupMandatory("generator", MessageGenerator.class);
        this.errorThreshold = params.getInteger("errorThreshold", -1);
        this.idleThreshold = params.getInteger("idleThreshold", -1);
        this.backoffMultiplier = params.getInteger("backoffMultiplier", -1);
        this.daemon = params.getBoolean("daemon", true);

        if (this.backoffMultiplier > 0 && this.errorThreshold < 0 && this.idleThreshold < 0)
            throw new IllegalArgumentException(
                    "errorThreshold and/or idleThreshold must be set when backoffMultiplier is set");
    }

    @Override
    protected void onStart() {
        var scheduler = executors.computeIfAbsent(schedulerName,
                key -> Executors.newScheduledThreadPool(threads, this::spawnThread));
        connRefs.computeIfAbsent(schedulerName, key -> new ConnectionRef<>(schedulerName)).ref();

        if (fixedRate)
            this.future = scheduler.scheduleAtFixedRate(this::poll, delay, period, TimeUnit.MILLISECONDS);
        else if (fixedDelay)
            this.future = scheduler.scheduleWithFixedDelay(this::poll, delay, period, TimeUnit.MILLISECONDS);
        else
            this.future = scheduler.schedule(this::poll, delay, TimeUnit.MILLISECONDS);
    }

    private Thread spawnThread(Runnable runnable) {
        var thread = new Thread(runnable);
        thread.setName("[Scheduler] " + schedulerName);
        thread.setDaemon(daemon);
        return thread;
    }

    private void poll() {
        if (shouldBackoff())
            return;

        var deferred = new AsyncDeferredObject<Message, Exception>();
        if (!publish(generator.get(), deferred)) {
            handleIdle();
            return;
        }
        deferred.done(this::handleResult) //
                .fail(this::handleException);
    }

    private boolean shouldBackoff() {
        if (backoffMultiplier < 0)
            return false;
        if ((errorThreshold < 0 || errorCounter.get() <= errorThreshold)
                && (idleThreshold < 0 || idleCounter.get() <= idleThreshold))
            return false;
        if (backoffCounter.incrementAndGet() <= backoffMultiplier) {
            log.debug("Number of errors or idles exceeds maximum allowed, trying to backoff");
            return true;
        }
        log.trace("Recovering from backoff");
        errorCounter.set(0);
        idleCounter.set(0);
        backoffCounter.set(0);
        return false;
    }

    private void handleIdle() {
        errorCounter.set(0);
        idleCounter.incrementAndGet();
    }

    private void handleResult(Object ignored) {
        errorCounter.set(0);
        idleCounter.set(0);
    }

    private void handleException(Exception ex) {
        log.error("Exception caught while running scheduler", ex);
        errorCounter.incrementAndGet();
        idleCounter.set(0);
    }

    private Message createDefaultMessage() {
        var headers = BObject.of("schedulerName", schedulerName) //
                             .setAny("timestamp", System.currentTimeMillis());
        return Message.ofAny(headers, null);
    }

    @Override
    protected void onStop() {
        future.cancel(false);
        var conn = connRefs.get(schedulerName);
        if (conn != null && conn.deref() == 0) {
            connRefs.remove(schedulerName);
            var scheduler = executors.remove(schedulerName);
            if (scheduler != null)
                scheduler.shutdown();
        }
    }

    @Override
    protected String generateName() {
        return "consumer.scheduler." + schedulerName;
    }
}
