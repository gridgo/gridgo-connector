package io.gridgo.connector.kafka.test;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;

import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.kafka.KafkaConnector;
import io.gridgo.connector.kafka.KafkaConstants;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class KafkaProducerUnitTest {

	private static final short REPLICATION_FACTOR = (short) 1;

	private static final int NUM_PARTITIONS = 1;

	private static final int NUM_MESSAGES = 100000;

	private static final int NUM_BROKERS = 1;

	@ClassRule
	public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource()
			.withBrokers(NUM_BROKERS).withBrokerProperty("auto.create.topics.enable", "false");

	@Test
	public void testProducerSend() {
		String extraQuery = "&mode=producer";
		String topicName = createTopic();

		String brokers = sharedKafkaTestResource.getKafkaConnectString();

		var connectString = "kafka:" + topicName + "?brokers=" + brokers + extraQuery;
		var connector = createKafkaConnector(connectString);
		var producer = connector.getProducer().orElseThrow();

		connector.start();

		String key = "test-key";
		String value = "test-message";
		BObject headers = BObject.newDefault().setAny(KafkaConstants.KEY, key).setAny(KafkaConstants.PARTITION, 0);
		Message msg = Message.newDefault(Payload.newDefault(headers, BValue.newDefault(value)));

		long started = System.nanoTime();

		for (int i = 0; i < NUM_MESSAGES; i++) {
			producer.send(msg);
		}

		long elapsed = System.nanoTime() - started;
		printPace("KafkaProducerSend", NUM_MESSAGES, elapsed);

		connector.stop();
	}

	@Test
	public void testProducerSendWithAck() {
		String extraQuery = "&mode=producer";

		String topicName = createTopic();

		String brokers = sharedKafkaTestResource.getKafkaConnectString();

		var connectString = "kafka:" + topicName + "?brokers=" + brokers + extraQuery;
		var connector = createKafkaConnector(connectString);
		var producer = connector.getProducer().orElseThrow();

		connector.start();

		String key = "test-key";
		String value = "test-message";
		BObject headers = BObject.newDefault().setAny(KafkaConstants.KEY, key).setAny(KafkaConstants.PARTITION, 0);
		Message msg = Message.newDefault(Payload.newDefault(headers, BValue.newDefault(value)));

		CountDownLatch latch = new CountDownLatch(NUM_MESSAGES);

		long started = System.nanoTime();

		for (int i = 0; i < NUM_MESSAGES; i++) {
			producer.sendWithAck(msg).done(response -> latch.countDown());
		}

		try {
			latch.await();
		} catch (InterruptedException e) {

		}

		long elapsed = System.nanoTime() - started;
		printPace("KafkaProducerSendWithAck", NUM_MESSAGES, elapsed);

		connector.stop();
	}

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

	private void printPace(String name, int numMessages, long elapsed) {
		DecimalFormat df = new DecimalFormat("###,###.##");
		System.out.println(name + ": " + numMessages + " operations were processed in " + df.format(elapsed / 1e6)
				+ "ms -> pace: " + df.format(1e9 * numMessages / elapsed) + "ops/s");
	}
}
