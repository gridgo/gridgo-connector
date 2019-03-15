package io.gridgo.socket.zmq.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Ignore;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class ZMQPubSubUnitTest {

    private static final String TEXT = "This is test text";

    private final ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

    private void testPubSub(String transport, String address) throws Exception {

        Connector pubConnector = RESOLVER.resolve("zmq:pub:" + transport + "://" + address);
        Connector sub1Connector = RESOLVER.resolve("zmq:sub:" + transport + "://" + address + "?topic=topic1");
        Connector sub2Connector = RESOLVER.resolve("zmq:sub:" + transport + "://" + address + "?topic=topic2");
        Connector sub3Connector = RESOLVER.resolve("zmq:sub:" + transport + "://" + address + "?topic=topic");

        try {
            pubConnector.start();
            assertTrue(pubConnector.getProducer().isPresent());

            sub1Connector.start();
            assertTrue(sub1Connector.getConsumer().isPresent());

            sub2Connector.start();
            assertTrue(sub2Connector.getConsumer().isPresent());

            sub3Connector.start();
            assertTrue(sub3Connector.getConsumer().isPresent());

            Producer publisher = pubConnector.getProducer().get();
            Consumer subscriber1 = sub1Connector.getConsumer().get();
            Consumer subscriber2 = sub2Connector.getConsumer().get();
            Consumer subscriber3 = sub3Connector.getConsumer().get();

            final String text1 = TEXT + 1;
            final String text2 = TEXT + 2;

            final AtomicReference<String> recv1 = new AtomicReference<String>();
            final AtomicReference<String> recv2 = new AtomicReference<String>();
            final Set<String> recv3 = new HashSet<>();

            final CountDownLatch doneSignal = new CountDownLatch(4);

            subscriber1.subscribe((msg) -> {
                String body = msg.body().asValue().getString();
                recv1.set(body);
                doneSignal.countDown();
            });

            subscriber2.subscribe((msg) -> {
                String body = msg.body().asValue().getString();
                recv2.set(body);
                doneSignal.countDown();
            });

            subscriber3.subscribe((msg) -> {
                String body = msg.body().asValue().getString();
                recv3.add(body);
                doneSignal.countDown();
            });

            // publish data
            publisher.send(Message.of(Payload.of(BValue.of(text1))).setRoutingIdFromAny("topic1"));
            publisher.send(Message.of(Payload.of(BValue.of(text2))).setRoutingIdFromAny("topic2"));

            doneSignal.await(5, TimeUnit.SECONDS);

            assertEquals(text1, recv1.get());
            assertEquals(text2, recv2.get());

            assertTrue(recv3.contains(text1));
            assertTrue(recv3.contains(text2));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            pubConnector.stop();
            sub1Connector.stop();
            sub2Connector.stop();
            sub3Connector.stop();
        }
    }

    @Test
    @Ignore
    public void testPubSubPGM() throws Exception {
        System.out.println("Test PGM protocol support");

        String transport = "epgm";
        String host = "239.192.1.1";
        int port = 5555;
        String address = host + ":" + port;

        Connector connector = RESOLVER.resolve("zmq:pub:" + transport + "://" + address);
        connector.start();
        assertTrue(connector.getProducer().isPresent());
        connector.stop();
    }

    @Test
    public void testPubSubTCP() throws Exception {
        System.out.println("Test pub/sub via TCP");
        testPubSub("tcp", "localhost:5555");
    }

}
