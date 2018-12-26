package io.gridgo.connector.redis.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.framework.support.Message;

public class RedisUnitTest {

    @Test
    public void testSetAndGet() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector("redis:single://[localhost:6379]");
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(BObject.of("cmd", "set"), BObject.of("key", "mykey").setAny("value", "1")))
                .fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                }).pipeDone(result -> {
                    return producer.call(Message.ofAny(BObject.of("cmd", "get"), "mykey"));
                }).always((s, r, e) -> {
                    if (e != null) {
                        exRef.set(e);
                    } else {
                        var body = r.getPayload().getBody();
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
