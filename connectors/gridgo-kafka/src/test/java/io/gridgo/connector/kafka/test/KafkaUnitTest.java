package io.gridgo.connector.kafka.test;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import com.salesforce.kafka.test.KafkaTestUtils;
import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;

import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.kafka.KafkaConnector;

public class KafkaUnitTest {

	@ClassRule
	public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource().withBrokers(2)
			.withBrokerProperty("auto.create.topics.enable", "false");

	@Test
	public void testConsumer() {

		String topicName = UUID.randomUUID().toString();

		var kafkaTestUtils = sharedKafkaTestResource.getKafkaTestUtils();
		kafkaTestUtils.createTopic(topicName, 1, (short) 1);

		String brokers = sharedKafkaTestResource.getKafkaConnectString();

		var connector = new DefaultConnectorFactory().createConnector("kafka:" + topicName + "?brokers=" + brokers
				+ "&consumersCount=1&autoCommitEnable=false&mode=consumer&groupId=test&autoOffsetReset=earliest");

		Assert.assertNotNull(connector);
		Assert.assertTrue(connector instanceof KafkaConnector);

		var consumer = connector.getConsumer().orElseThrow();

		int numMessages = 1000;

		var latch = new CountDownLatch(numMessages);

		consumer.clearSubscribers();
		consumer.subscribe((msg, deferred) -> {
			latch.countDown();
			deferred.resolve(null);
		});

		sendTestRecords(topicName, kafkaTestUtils, numMessages);

		connector.start();

		long started = System.nanoTime();
		try {
			latch.await();
		} catch (InterruptedException e) {

		}
		long elapsed = System.nanoTime() - started;
		printPace("KafkaConsumer", numMessages, elapsed);

		connector.stop();
	}

	private void sendTestRecords(String topicName, KafkaTestUtils kafkaTestUtils, int numMessages) {
		System.out.println("Sending records...");
		// Define our message
		final int partitionId = 0;
		final String expectedKey = "my-key";
		final String expectedValue = "my test message";

		var producerRecord = new ProducerRecord<String, String>(topicName, partitionId, expectedKey, expectedValue);

		long started = System.nanoTime();
		// Create a new producer
		try (var producer = kafkaTestUtils.getKafkaProducer(StringSerializer.class, StringSerializer.class)) {

			// Produce it & wait for it to complete.
			for (int i = 0; i < numMessages; i++)
				producer.send(producerRecord);
			// producer.flush();
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
