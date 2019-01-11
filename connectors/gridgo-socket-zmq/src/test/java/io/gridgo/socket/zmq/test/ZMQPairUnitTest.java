package io.gridgo.socket.zmq.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class ZMQPairUnitTest {

    private static final String TEXT = "This is test text";

    private static final int port = 8080;

    private static final String host = "localhost";
    private static final String address = host + ":" + port;
    private final ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

    @Test
    public void testPairOneWay() throws InterruptedException, PromiseException {

        String osName = System.getProperty("os.name");
        if (osName != null && osName.contains("Windows"))
            return;

        Connector connector1 = RESOLVER.resolve("zmq:pair:tcp:bind://" + address);
        Connector connector2 = RESOLVER.resolve("zmq:pair:tcp:connect://" + address);

        connector1.start();
        assertTrue(connector1.getConsumer().isPresent());
        assertTrue(connector1.getProducer().isPresent());

        connector2.start();
        assertTrue(connector2.getConsumer().isPresent());
        assertTrue(connector2.getProducer().isPresent());

        final CountDownLatch doneSignal = new CountDownLatch(2);

        AtomicReference<String> recvDataRef1 = new AtomicReference<String>(null);
        AtomicReference<String> recvDataRef2 = new AtomicReference<String>(null);

        connector1.getConsumer().get().subscribe((message) -> {
            recvDataRef2.set(message.body().asValue().getString());
            doneSignal.countDown();
        });

        connector2.getConsumer().get().subscribe((message) -> {
            recvDataRef1.set(message.body().asValue().getString());
            doneSignal.countDown();
        });

        connector1.getProducer().get().send(Message.of(Payload.of(BValue.of(TEXT + 1))));
        connector2.getProducer().get().send(Message.of(Payload.of(BValue.of(TEXT + 2))));

        doneSignal.await();

        assertEquals(TEXT + 1, recvDataRef1.get());
        assertEquals(TEXT + 2, recvDataRef2.get());

        connector1.stop();
        connector2.stop();
    }

    @Test
    public void testPairPingPong() throws InterruptedException, PromiseException {

        String osName = System.getProperty("os.name");
        if (osName != null && osName.contains("Windows"))
            return;
        System.out.println("Test pair ping pong...");

        Connector connector1 = RESOLVER.resolve("zmq:pair:tcp:bind://" + address);
        Connector connector2 = RESOLVER.resolve("zmq:pair:tcp:connect://" + address);

        try {
            connector1.start();
            assertTrue(connector1.getConsumer().isPresent());
            assertTrue(connector1.getProducer().isPresent());

            connector2.start();
            assertTrue(connector2.getConsumer().isPresent());
            assertTrue(connector2.getProducer().isPresent());

            Producer responder = connector1.getProducer().get();
            connector1.getConsumer().get().subscribe((message) -> {
                responder.send(message);
            });

            AtomicReference<String> pongDataRef = new AtomicReference<String>(null);

            final CountDownLatch doneSignal = new CountDownLatch(1);
            connector2.getConsumer().get().subscribe((message) -> {
                pongDataRef.set(message.body().asValue().getString());
                doneSignal.countDown();
            });

            connector2.getProducer().get().send(Message.of(Payload.of(BValue.of(TEXT))));

            doneSignal.await(5, TimeUnit.SECONDS);
            assertEquals(TEXT, pongDataRef.get());

        } finally {
            connector1.stop();
            connector2.stop();
        }

    }
}
