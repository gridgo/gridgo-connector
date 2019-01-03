package io.gridgo.socket.zmq.test;

import static org.junit.Assert.assertTrue;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class ZMQPushPullUnitTest {

    private static final int port = 8080;

    private static final String host = "localhost";
    private static final String address = host + ":" + port;
    private final ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

    private void doAckSend(Consumer consumer, Producer producer) throws InterruptedException, PromiseException {
        int numMessages = (int) 1e2;
        CountDownLatch doneSignal = new CountDownLatch(numMessages);

        consumer.subscribe((message) -> {
            doneSignal.countDown();
        });

        long start = System.nanoTime();
        for (int i = 0; i < numMessages; i++) {
            producer.sendWithAck(Message.of(Payload.of(BObject.ofSequence("index", i)))).get();
        }

        if (doneSignal.await(1, TimeUnit.MINUTES)) {
            double elapsed = Double.valueOf(System.nanoTime() - start);
            DecimalFormat df = new DecimalFormat("###,###.##");
            System.out.println("ACK TRANSMITION DONE (*** not improved), " + numMessages + " messages were transmited in " + df.format(elapsed / 1e6)
                    + "ms -> pace: " + df.format(1e9 * numMessages / elapsed) + "msg/s");

            consumer.clearSubscribers();
        } else {
            consumer.clearSubscribers();
            throw new RuntimeException("Test cannot be done after 1 min");
        }

    }

    private void doFnFSend(Consumer consumer, Producer producer) throws InterruptedException {
        int numMessages = (int) 1e2;
        CountDownLatch doneSignal = new CountDownLatch(numMessages);

        consumer.subscribe((message) -> {
            doneSignal.countDown();
        });
        long start = System.nanoTime();
        for (int i = 0; i < numMessages; i++) {
            producer.send(Message.of(Payload.of(BObject.ofSequence("index", i))));
        }

        if (doneSignal.await(1, TimeUnit.MINUTES)) {
            double elapsed = Double.valueOf(System.nanoTime() - start);
            DecimalFormat df = new DecimalFormat("###,###.##");
            System.out.println("FnF TRANSMITION DONE (*** not improved), " + numMessages + " messages were transmited in " + df.format(elapsed / 1e6)
                    + "ms -> pace: " + df.format(1e9 * numMessages / elapsed) + "msg/s");
            consumer.clearSubscribers();
        } else {
            consumer.clearSubscribers();
            throw new RuntimeException("Test cannot be done after 1 min");
        }

    }

    @Test
    public void testMonoplex() throws InterruptedException, PromiseException {

        String osName = System.getProperty("os.name");
        if (osName != null && osName.contains("Windows"))
            return;

        System.out.println("Test tcp monoplex...");
        String queryString = "batchingEnabled=true&maxBatchSize=2000&ringBufferSize=2048";

        Connector connector1 = RESOLVER.resolve("zmq:pull:tcp://" + address);
        Connector connector2 = RESOLVER.resolve("zmq:push:tcp://" + address + "?" + queryString);

        try {
            connector1.start();
            assertTrue(connector1.getConsumer().isPresent());
            Consumer consumer = connector1.getConsumer().get();

            connector2.start();
            assertTrue(connector2.getProducer().isPresent());
            Producer producer = connector2.getProducer().get();

            warmUp(consumer, producer);

            this.doFnFSend(consumer, producer);
            this.doAckSend(consumer, producer);
        } finally {
            connector1.stop();
            connector2.stop();
        }
    }

    private void warmUp(Consumer consumer, Producer producer) throws PromiseException, InterruptedException {
        System.out.println("Started consumer and producer");
        CountDownLatch doneSignal = new CountDownLatch(1);
        consumer.subscribe((msg) -> {
            System.out.println("Got message from source: " + msg.getMisc().get("source"));
            doneSignal.countDown();
        });
        producer.send(Message.of(Payload.of(BObject.ofSequence("cmd", "start"))));
        doneSignal.await();
        System.out.println("Warmup done");
        consumer.clearSubscribers();
    }
}
