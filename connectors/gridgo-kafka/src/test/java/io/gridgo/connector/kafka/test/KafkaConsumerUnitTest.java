package io.gridgo.connector.kafka.test;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;

import io.gridgo.connector.Connector;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.kafka.KafkaConnector;
import io.gridgo.connector.kafka.KafkaConstants;

public class KafkaConsumerUnitTest {

	private static final short REPLICATION_FACTOR = (short) 1;

	private static final int NUM_PARTITIONS = 1;

	private static final int NUM_MESSAGES = 100;

	private static final double ERROR_RATE = 0.02;

	@ClassRule
	public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource().withBrokers(1)
			.withBrokerProperty("auto.create.topics.enable", "false");

	@Test
	public void testConsumer() {

		String extraQuery = "&consumersCount=1&autoCommitEnable=false&mode=consumer&groupId=test&autoOffsetReset=earliest";

		doTestConsumer(extraQuery);
	}

	@Test
	public void testAutoCommitSyncConsumer() {

		String extraQuery = "&consumersCount=1&autoCommitEnable=true&autoCommitOnStop=sync&mode=consumer&groupId=test&autoOffsetReset=earliest";

		doTestConsumer(extraQuery);
	}

	@Test
	public void testAutoCommitAsyncConsumer() {

		String extraQuery = "&consumersCount=1&autoCommitEnable=true&autoCommitOnStop=async&mode=consumer&groupId=test&autoOffsetReset=earliest";

		doTestConsumer(extraQuery);
	}

	private void doTestConsumer(String extraQuery) {
		String topicName = createTopic();

		String brokers = sharedKafkaTestResource.getKafkaConnectString();

		var connectString = "kafka:" + topicName + "?brokers=" + brokers + extraQuery;
		var connector = createKafkaConnector(connectString);

		var consumer = connector.getConsumer().orElseThrow();

		var latch = new CountDownLatch(NUM_MESSAGES);

		consumer.clearSubscribers();
		consumer.subscribe((msg, deferred) -> {
			latch.countDown();
			deferred.resolve(null);
		});

		sendTestRecords(topicName, NUM_MESSAGES);

		connector.start();

		long started = System.nanoTime();
		try {
			latch.await();
		} catch (InterruptedException e) {

		}
		long elapsed = System.nanoTime() - started;
		printPace("KafkaConsumer", NUM_MESSAGES, elapsed);

		connector.stop();
	}

	@Test
	public void testConsumerWithError() {

		doTestConsumerWithError(
				"&consumersCount=1&autoCommitEnable=false&mode=consumer&groupId=test&autoOffsetReset=earliest");
	}

	@Test
	public void testMultiConsumerWithError() {

		doTestConsumerWithError(
				"&consumersCount=2&autoCommitEnable=false&mode=consumer&groupId=test&autoOffsetReset=earliest");
	}

	private void doTestConsumerWithError(String extraQuery) {
		String topicName = createTopic();

		String brokers = sharedKafkaTestResource.getKafkaConnectString();

		var connectString = "kafka:" + topicName + "?brokers=" + brokers + extraQuery;
		var connector = createKafkaConnector(connectString);

		var consumer = connector.getConsumer().orElseThrow();

		var latch = ConcurrentHashMap.<Integer>newKeySet();
		AtomicInteger atomic = new AtomicInteger(0);
		for (int i = 0; i < NUM_MESSAGES; i++) {
			latch.add(i);
		}

		consumer.clearSubscribers();
		consumer.subscribe((msg, deferred) -> {
			int body = msg.getPayload().getBody().asValue().getInteger();
			int counter = atomic.incrementAndGet();
			if (counter % (1 / ERROR_RATE) == 0) {
				deferred.reject(new RuntimeException("just fail (" + body + ")"));
				return;
			}
			if (!latch.contains(body)) {
				System.out.println("Duplicate message: " + body);
			} else {
				latch.remove(body);
			}
			deferred.resolve(null);
		});

		sendTestRecords(topicName, NUM_MESSAGES);

		connector.start();

		long started = System.nanoTime();
		while (!latch.isEmpty()) {
			Thread.onSpinWait();
		}
		long elapsed = System.nanoTime() - started;
		printPace("KafkaConsumer", NUM_MESSAGES, elapsed);

		connector.stop();
	}

	@Test
	public void testBatchConsumer() {

		String topicName = createTopic();

		String brokers = sharedKafkaTestResource.getKafkaConnectString();

		var connectString = "kafka:" + topicName + "?brokers=" + brokers
				+ "&consumersCount=1&autoCommitEnable=false&mode=consumer&groupId=test&autoOffsetReset=earliest&batchEnabled=true";
		var connector = createKafkaConnector(connectString);

		var consumer = connector.getConsumer().orElseThrow();

		var latch = new AtomicInteger(NUM_MESSAGES);

		consumer.clearSubscribers();
		consumer.subscribe((msg, deferred) -> {
			int processed = msg.getPayload().getHeaders().getInteger(KafkaConstants.BATCH_SIZE);
			latch.addAndGet(-processed);
			deferred.resolve(null);
		});

		sendTestRecords(topicName, NUM_MESSAGES);

		connector.start();

		long started = System.nanoTime();
		while (latch.get() != 0) {
			Thread.onSpinWait();
		}
		long elapsed = System.nanoTime() - started;
		printPace("KafkaConsumer", NUM_MESSAGES, elapsed);

		connector.stop();
	}

	private String createTopic() {
		String topicName = UUID.randomUUID().toString();

		var kafkaTestUtils = sharedKafkaTestResource.getKafkaTestUtils();
		kafkaTestUtils.createTopic(topicName, NUM_PARTITIONS, REPLICATION_FACTOR);
		return topicName;
	}

	private Connector createKafkaConnector(String connectString) {
		var connector = new DefaultConnectorFactory().createConnector(connectString);

		Assert.assertNotNull(connector);
		Assert.assertTrue(connector instanceof KafkaConnector);
		return connector;
	}

	private void sendTestRecords(String topicName, int numMessages) {
		var kafkaTestUtils = sharedKafkaTestResource.getKafkaTestUtils();

		System.out.println("Sending records...");

		long started = System.nanoTime();
		// Create a new producer
		try (var producer = kafkaTestUtils.getKafkaProducer(StringSerializer.class, StringSerializer.class)) {

			// Produce it & wait for it to complete.
			for (int i = 0; i < numMessages; i++) {
				var producerRecord = new ProducerRecord<String, String>(topicName, i + "", i + "");
				producer.send(producerRecord);
			}
			producer.flush();
		}
		long elapsed = System.nanoTime() - started;
		printPace("KafkaEmbeddedProducer", numMessages, elapsed);
	}

	private void printPace(String name, int numMessages, long elapsed) {
		DecimalFormat df = new DecimalFormat("###,###.##");
		System.out.println(name + ": " + numMessages + " operations were processed in " + df.format(elapsed / 1e6)
				+ "ms -> pace: " + df.format(1e9 * numMessages / elapsed) + "ops/s");
	}
}
