package io.gridgo.connector.file.test;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.connector.file.FileProducer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.execution.impl.ExecutorExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class FileConsumerUnitTest {

    private static final int NUM_MESSAGES = 2;
    private static final int LIMIT = 100;

    private long appendFile(Message msg, int numMessages, String endpoint) throws InterruptedException {
        var connector1 = new DefaultConnectorFactory().createConnector(endpoint);
        connector1.start();

        var producer = (FileProducer) connector1.getProducer().orElseThrow();
        var latch = new CountDownLatch(numMessages);
        for (int i = 0; i < numMessages; i++) {
            producer.sendWithAck(msg).always((s, r, e) -> latch.countDown());
        }
        latch.await();
        connector1.stop();
        return producer.getEngine().getTotalSentBytes();
    }

    private void doTestFile(String scheme, String format, String batchEnabled, String lengthPrepend) throws InterruptedException {
        var totalSentBytes = prepareFile(scheme, format, batchEnabled, lengthPrepend);

        var endpoint = scheme + "://[test." + lengthPrepend + "." + batchEnabled + "." + format + "]?format=" + format + "&limitStrategy=rotate&limitSize="
                + LIMIT + "&deleteOnShutdown=true&lengthPrepend=" + lengthPrepend;
        var strategy = new ExecutorExecutionStrategy(1);
        var context = new DefaultConnectorContextBuilder().setConsumerExecutionStrategy(strategy).build();
        var connector = new DefaultConnectorFactory().createConnector(endpoint, context);
        var consumer = connector.getConsumer().orElseThrow();
        var latch = new CountDownLatch(NUM_MESSAGES);
        var exRef = new AtomicReference<>();
        consumer.subscribe(msg -> {
            var response = msg.body().asValue().getString();
            if (!"hello".equals(response))
                exRef.set(new RuntimeException("Expected: hello. Actual: " + response));
            latch.countDown();
        });
        var start = System.nanoTime();
        connector.start();
        latch.await();

        var elapsed = System.nanoTime() - start;
        printPace("Consumer (format=" + format + ", batchingEnabled=" + batchEnabled + ", lengthPrepend=" + lengthPrepend + ")\n", NUM_MESSAGES, elapsed,
                totalSentBytes);

        connector.stop();
        strategy.stop();
        Assert.assertNull(exRef.get());
    }

    private long prepareFile(String scheme, String format, String batchEnabled, String lengthPrepend) throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(scheme + "://[test." + lengthPrepend + "." + batchEnabled + "." + format + "]?format="
                + format + "&batchingEnabled=" + batchEnabled + "&lengthPrepend=" + lengthPrepend + "&limitStrategy=rotate&limitSize=" + LIMIT
                + "&deleteOnStartup=true&maxBatchSize=1000&producerOnly=true");
        connector.start();

        var producer = (FileProducer) connector.getProducer().orElseThrow();

        var msg = Message.of(Payload.of(BValue.of("hello")));

        var latch = new CountDownLatch(NUM_MESSAGES);

        for (int i = 0; i < NUM_MESSAGES; i++) {
            producer.sendWithAck(msg).always((s, r, e) -> latch.countDown());
        }

        latch.await();

        connector.stop();

        return producer.getEngine().getTotalSentBytes();
    }

    private void printPace(String name, int numMessages, long elapsed, long totalSentBytes) {
        DecimalFormat df = new DecimalFormat("###,###.##");
        System.out.println("Total sent bytes: " + df.format(totalSentBytes));
        System.out.println(name + ": " + numMessages + " operations were processed in " + df.format(elapsed / 1e6) + "ms -> pace: "
                + df.format(1e9 * numMessages / elapsed) + "ops/s" + " with bandwidth of " + df.format(1e9 * totalSentBytes / elapsed / 1024 / 1024) + "MB/s");
    }

    @Test
    public void testBatchWithLengthPrepend() throws InterruptedException {
        System.out.println("Test batching with length prepend\n");
        doTestFile("file:disruptor", "xml", "true", "true");
        doTestFile("file:disruptor", "json", "true", "true");
        doTestFile("file:disruptor", "raw", "true", "true");
        System.out.println("-----");
    }

    @Test
    public void testMultiConnector() throws InterruptedException {
        var msg = Message.of(Payload.of(BValue.of("hello")));

        var numMessages = 1;
        var endpoint1 = "file://[testTwice.txt]?format=raw&batchingEnabled=true&lengthPrepend=true&deleteOnStartup=true&producerOnly=true";

        appendFile(msg, numMessages, endpoint1);

        var endpoint2 = "file://[testTwice.txt]?format=raw&batchingEnabled=true&lengthPrepend=true&producerOnly=true";
        appendFile(msg, numMessages, endpoint2);

        var endpoint = "file://[testTwice.txt]?format=raw&deleteOnShutdown=true&lengthPrepend=true";
        var strategy = new ExecutorExecutionStrategy(1);
        var context = new DefaultConnectorContextBuilder().setConsumerExecutionStrategy(strategy).build();
        var connector = new DefaultConnectorFactory().createConnector(endpoint, context);
        var consumer = connector.getConsumer().orElseThrow();
        var latch = new CountDownLatch(numMessages * 2);
        var exRef = new AtomicReference<>();
        consumer.subscribe(request -> {
            var response = request.body().asValue().getString();
            if (!"hello".equals(response))
                exRef.set(new RuntimeException("Expected: hello. Actual: " + response));
            latch.countDown();
        });
        connector.start();
        latch.await();

        connector.stop();
        strategy.stop();
        Assert.assertNull(exRef.get());
    }

    @Test
    public void testNonBatchWithLengthPrepend() throws InterruptedException {
        System.out.println("Test non-batching with length prepend\n");
        doTestFile("file:disruptor", "xml", "false", "true");
        doTestFile("file:disruptor", "json", "false", "true");
        doTestFile("file:disruptor", "raw", "false", "true");
        System.out.println("-----");
    }
}
