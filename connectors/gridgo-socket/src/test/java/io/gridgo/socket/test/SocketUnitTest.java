package io.gridgo.socket.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class SocketUnitTest {

    private static final int NUM_MESSAGES = 100;

    @Test
    public void testBatchSocket() throws InterruptedException {
        var factory = new DefaultConnectorFactory(new ClasspathConnectorResolver("io.gridgo.socket"));
        var connector1 = factory.createConnector("testsocket:pull:tcp://127.0.0.1:9102?batchingEnabled=true");
        var connector2 = factory.createConnector("testsocket:push:tcp://127.0.0.1:9102?batchingEnabled=true");

        var latch = new CountDownLatch(NUM_MESSAGES);

        Assert.assertTrue(connector1.getProducer().isEmpty());
        Assert.assertTrue(connector1.getConsumer().isPresent());

        Assert.assertTrue(connector2.getConsumer().isEmpty());
        Assert.assertTrue(connector2.getProducer().isPresent());

        var exRef = new AtomicInteger();

        connector1.getConsumer().get().subscribe(msg -> {
            if (msg.body().isValue())
                exRef.set(msg.body().asValue().getInteger());
            latch.countDown();
        });

        connector1.start();
        connector2.start();

        for (int i = 0; i < NUM_MESSAGES; i++)
            connector2.getProducer().get() //
                      .send(Message.of(Payload.of(BValue.of(1))));

        latch.await();
        connector2.stop();
        connector1.stop();
    }

    @Test
    public void testSocket() throws InterruptedException {
        var factory = new DefaultConnectorFactory(new ClasspathConnectorResolver("io.gridgo.socket"));
        var connector1 = factory.createConnector("testsocket:pull:tcp://127.0.0.1:9102");
        var connector2 = factory.createConnector("testsocket:push:tcp://127.0.0.1:9102");

        var latch = new CountDownLatch(1);

        Assert.assertTrue(connector1.getProducer().isEmpty());
        Assert.assertTrue(connector1.getConsumer().isPresent());

        Assert.assertTrue(connector2.getConsumer().isEmpty());
        Assert.assertTrue(connector2.getProducer().isPresent());

        var exRef = new AtomicInteger();

        connector1.getConsumer().get().subscribe(msg -> {
            if (msg.body().isValue())
                exRef.set(msg.body().asValue().getInteger());
            latch.countDown();
        });

        connector1.start();
        connector2.start();

        connector2.getProducer().get().send(Message.of(Payload.of(BValue.of(1))));

        latch.await();

        connector2.stop();
        connector1.stop();
    }
}
