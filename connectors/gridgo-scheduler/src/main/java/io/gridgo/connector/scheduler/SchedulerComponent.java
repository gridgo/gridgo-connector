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
public class SchedulerComponent extends AbstractConsumer {

    private static final Map<String, ScheduledExecutorService> executors = new NonBlockingHashMap<>();

    private static final Map<String, ConnectionRef<String>> connRefs = new NonBlockingHashMap<>();

    private String schedulerName;

    private Integer threads;

    private Long delay;

    private Long period;

    private Boolean fixedRate;

    private Boolean fixedDelay;

    private Integer errorThreshold;

    private Integer backoffMultiplier;

    private ScheduledFuture<?> future;

    private Supplier<Message> generator = this::createDefaultMessage;

    private AtomicInteger errorCounter = new AtomicInteger();

    private AtomicInteger backoffCounter = new AtomicInteger();

    private boolean daemon;

    public SchedulerComponent(ConnectorContext context, String name, BObject params) {
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
        this.backoffMultiplier = params.getInteger("backoffMultiplier", -1);
        this.daemon = params.getBoolean("daemon", true);

        if (this.backoffMultiplier > 0 && this.errorThreshold < 0)
            throw new IllegalArgumentException("errorThreshold must be set when backoffMultiplier is set");
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
        publish(generator.get(), deferred);
        deferred.done(r -> errorCounter.set(0)) //
                .fail(this::handleException);
    }

    private boolean shouldBackoff() {
        if (backoffMultiplier < 0 || errorCounter.get() <= errorThreshold)
            return false;
        if (backoffCounter.incrementAndGet() <= backoffMultiplier) {
            log.debug("Number of errors exceeds maximum allowed, trying to backoff");
            return true;
        }
        log.trace("Recovering from backoff");
        errorCounter.set(0);
        backoffCounter.set(0);
        return false;
    }

    private void handleException(Exception ex) {
        log.error("Exception caught while running scheduler", ex);
        errorCounter.incrementAndGet();
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
