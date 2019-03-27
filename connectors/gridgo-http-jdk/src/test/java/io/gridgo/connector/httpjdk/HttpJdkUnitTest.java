package io.gridgo.connector.httpjdk;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;

public class HttpJdkUnitTest {

    private static final int NUM_MESSAGES = 1;

    @Test
    public void testSend() throws InterruptedException {
        var connector = createConnector();
        connector.start();
        var producer = connector.getProducer().orElseThrow();

        warmUp(producer);

        var start = System.nanoTime();
        for (int i = 0; i < NUM_MESSAGES; i++) {
            producer.send(null);
        }

        var elapsed = System.nanoTime() - start;
        printPace("Http Jdk send", NUM_MESSAGES, elapsed);

        connector.stop();
    }

    @Test
    public void testSendWithAck() throws InterruptedException {
        var connector = createConnector();
        connector.start();
        var producer = connector.getProducer().orElseThrow();
        var latch = new CountDownLatch(NUM_MESSAGES);
        var atomic = new AtomicInteger(0);
        var ref = new AtomicReference<>();

        warmUp(producer);

        var start = System.nanoTime();
        for (int i = 0; i < NUM_MESSAGES; i++) {
            producer.sendWithAck(null) //
                    .fail(ex -> {
                        ref.set(ex);
                        atomic.incrementAndGet();
                    }) //
                    .always((s, r, e) -> latch.countDown());
        }

        latch.await();

        var elapsed = System.nanoTime() - start;
        printPace("Http Jdk sendWithAck", NUM_MESSAGES, elapsed);

        connector.stop();

        Assert.assertNull(ref.get());
        Assert.assertEquals(0, atomic.get());
    }

    private void warmUp(Producer producer) throws InterruptedException {
        var warmUpLatch = new CountDownLatch(1);
        producer.sendWithAck(null).always((s, r, e) -> warmUpLatch.countDown());
        warmUpLatch.await();
    }

    @Test
    public void testCall() throws InterruptedException {
        var connector = createConnector();
        connector.start();
        var producer = connector.getProducer().orElseThrow();
        var latch = new CountDownLatch(NUM_MESSAGES);
        var atomic = new AtomicInteger(0);
        var ref = new AtomicReference<>();

        warmUp(producer);

        var start = System.nanoTime();
        for (int i = 0; i < NUM_MESSAGES; i++) {
            producer.call(null) //
                    .done(response -> {
                        var body = response.body().asValue().getString();
                        if (!"hello".equals(body)) {
                            atomic.incrementAndGet();
                            ref.set(new RuntimeException(body));
                        }
                    }).fail(ex -> {
                        ref.set(ex);
                        atomic.incrementAndGet();
                    }) //
                    .always((s, r, e) -> latch.countDown());
        }

        latch.await();

        var elapsed = System.nanoTime() - start;
        printPace("Http Jdk call", NUM_MESSAGES, elapsed);

        connector.stop();

        Assert.assertNull(ref.get());
        Assert.assertEquals(0, atomic.get());
    }

    private Connector createConnector() {
        var connector = new DefaultConnectorFactory().createConnector(
                "https2://raw.githubusercontent.com/gridgo/gridgo-connector/dungba/developing/connectors/gridgo-http/src/test/resources/test.txt");
        return connector;
    }

    private void printPace(String name, int numMessages, long elapsed) {
        DecimalFormat df = new DecimalFormat("###,###.##");
        System.out.println(name + ": " + numMessages + " operations were processed in " + df.format(elapsed / 1e6)
                + "ms -> pace: " + df.format(1e9 * numMessages / elapsed) + "ops/s");
    }
}
