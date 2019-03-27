package io.gridgo.connector.redis.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.framework.support.Message;
import io.gridgo.redis.command.RedisCommands;

public abstract class RedisUnitTest {

    private static final String CMD = "cmd";

    private BObject buildCommand(String command) {
        return BObject.of(CMD, command);
    }

    protected abstract String getEndpoint();

    public void testAppend() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", "1"))).fail(e -> {
            exRef.set(e);
            latch.countDown();
        }).pipeDone(result -> {
            return producer.call(Message.ofAny(buildCommand(RedisCommands.APPEND), BArray.ofSequence("mykey", "2")));
        }).pipeDone(result -> {
            return producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "mykey"));
        }).always((s, r, e) -> {
            if (e != null) {
                exRef.set(e);
            } else {
                var body = r.body();
                if (!body.isValue() || !"12".equals(new String(body.asValue().getRaw()))) {
                    exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                }
            }
            latch.countDown();
        });
        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSetAndGet() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(BObject.of(CMD, "set"), BArray.ofSequence("mykey", "1"))).fail(e -> {
            exRef.set(e);
            latch.countDown();
        }).pipeDone(result -> {
            return producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "mykey"));
        }).always((s, r, e) -> {
            if (e != null) {
                exRef.set(e);
            } else {
                var body = r.body();
                if (!body.isValue() || !"1".equals(new String(body.asValue().getRaw()))) {
                    exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                }
            }
            latch.countDown();
        });
        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }
}
