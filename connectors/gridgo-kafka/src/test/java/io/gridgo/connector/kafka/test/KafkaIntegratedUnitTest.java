package io.gridgo.connector.kafka.test;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;

import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.kafka.KafkaConnector;
import io.gridgo.connector.kafka.KafkaConstants;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class KafkaIntegratedUnitTest {

    private static final short REPLICATION_FACTOR = (short) 1;

    private static final int NUM_PARTITIONS = 1;

    private static final int NUM_MESSAGES = 1;

    @ClassRule
    public static final SharedKafkaTestResource sharedKafkaTestResource = //
            new SharedKafkaTestResource().withBrokers(1) //
                                         .withBrokerProperty("auto.create.topics.enable", "false");

    private Connector createKafkaConnector(String connectString) {
        var connector = new DefaultConnectorFactory().createConnector(connectString);

        Assert.assertNotNull(connector);
        Assert.assertTrue(connector instanceof KafkaConnector);
        return connector;
    }

    private String createTopic() {
        String topicName = UUID.randomUUID().toString();

        var kafkaTestUtils = sharedKafkaTestResource.getKafkaTestUtils();
        kafkaTestUtils.createTopic(topicName, NUM_PARTITIONS, REPLICATION_FACTOR);
        return topicName;
    }

    private void doTestConsumerAndProducer(String extraQuery) {
        String topicName = createTopic();

        String brokers = sharedKafkaTestResource.getKafkaConnectString();

        var connectString = "kafka:" + topicName + "?brokers=" + brokers + extraQuery;
        var connector = createKafkaConnector(connectString);

        var consumer = connector.getConsumer().orElseThrow();

        System.out.println("Warming up...");
        var warmUpLatch = new CountDownLatch(1);

        connector.start();

        consumer.clearSubscribers();
        consumer.subscribe((msg, deferred) -> {
            warmUpLatch.countDown();
            deferred.resolve(null);
        });

        var producer = connector.getProducer().orElseThrow();

        sendTestRecords(topicName, producer, 1);

        try {
            warmUpLatch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        System.out.println("Warm up done");

        var latch = new AtomicInteger(NUM_MESSAGES);

        consumer.clearSubscribers();
        consumer.subscribe((msg, deferred) -> {
            int size = msg.headers().getInteger(KafkaConstants.BATCH_SIZE, 1);
            latch.addAndGet(-size);
            deferred.resolve(null);
        });

        long started = System.nanoTime();

        sendTestRecords(topicName, producer, NUM_MESSAGES);

        while (latch.get() != 0) {
            // Thread.onSpinWait();
            LockSupport.parkNanos(0);
        }
        long elapsed = System.nanoTime() - started;
        printPace("KafkaConsumer", NUM_MESSAGES, elapsed);

        connector.stop();

        System.out.println("Connector stop");
    }

    private void printPace(String name, int numMessages, long elapsed) {
        DecimalFormat df = new DecimalFormat("###,###.##");
        System.out.println(name + ": " + numMessages + " operations were processed in " + df.format(elapsed / 1e6)
                + "ms -> pace: " + df.format(1e9 * numMessages / elapsed) + "ops/s");
    }

    private void sendTestObjectRecords(String topicName, Producer producer, int numMessages) {
        System.out.println("Sending records...");

        long started = System.nanoTime();
        // Produce it & wait for it to complete.
        for (int i = 0; i < numMessages; i++) {
            var msg = Message.ofAny(BObject.of("test", 1));
            producer.send(msg);
        }
        long elapsed = System.nanoTime() - started;
        printPace("KafkaProducer", numMessages, elapsed);
    }

    private void sendTestRecords(String topicName, Producer producer, int numMessages) {
        System.out.println("Sending records...");

        long started = System.nanoTime();
        // Produce it & wait for it to complete.
        for (int i = 0; i < numMessages; i++) {
            Message msg = Message.of(Payload.of(BValue.of(i + "")));
            producer.send(msg);
        }
        long elapsed = System.nanoTime() - started;
        printPace("KafkaProducer", numMessages, elapsed);
    }

    @Test
    public void testBatchConsumerAndProducer() {

        String extraQuery = "&consumersCount=1&autoCommitEnable=false&groupId=test&autoOffsetReset=earliest&batchEnabled=true";

        doTestConsumerAndProducer(extraQuery);
    }

    @Test
    public void testConsumerAndProducer() {

        String extraQuery = "&consumersCount=1&autoCommitEnable=false&groupId=test&autoOffsetReset=earliest";

        doTestConsumerAndProducer(extraQuery);
    }

    @Test
    public void testConsumerAndProducerWithObject() {

        String extraQuery = "&consumersCount=1&autoCommitEnable=false&groupId=test&autoOffsetReset=earliest";

        String topicName = createTopic();

        String brokers = sharedKafkaTestResource.getKafkaConnectString();

        var connectString = "kafka:" + topicName + "?brokers=" + brokers + extraQuery;
        var connector = createKafkaConnector(connectString);

        var consumer = connector.getConsumer().orElseThrow();

        System.out.println("Warming up...");
        var warmUpLatch = new CountDownLatch(1);

        connector.start();

        consumer.clearSubscribers();
        consumer.subscribe((msg, deferred) -> {
            warmUpLatch.countDown();
            deferred.resolve(null);
        });

        var producer = connector.getProducer().orElseThrow();

        sendTestObjectRecords(topicName, producer, 1);

        try {
            warmUpLatch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        System.out.println("Warm up done");

        var latch = new AtomicInteger(NUM_MESSAGES);
        var exRef = new AtomicReference<>();

        consumer.clearSubscribers();
        consumer.subscribe((msg, deferred) -> {
            if (msg.body().isObject() && msg.body().asObject().getInteger("test") == 1) {
                int size = msg.headers().getInteger(KafkaConstants.BATCH_SIZE, 1);
                latch.addAndGet(-size);
            } else {
                exRef.set(new IllegalArgumentException(msg.body().toString()));
                latch.set(0);
            }
            deferred.resolve(null);
        });

        long started = System.nanoTime();

        sendTestObjectRecords(topicName, producer, NUM_MESSAGES);

        while (latch.get() != 0) {
            // Thread.onSpinWait();
            LockSupport.parkNanos(0);
        }
        long elapsed = System.nanoTime() - started;
        printPace("KafkaConsumer", NUM_MESSAGES, elapsed);

        connector.stop();

        System.out.println("Connector stop");

        Assert.assertNull(exRef.get());
    }
}
