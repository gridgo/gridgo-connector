package io.gridgo.connector.kafka;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.connector.impl.factories.DefaultConnectorFactory;

public class KafkaUnitTest {

	@Test
	public void testSimple() {
		var connector = new DefaultConnectorFactory()
				.createConnector("kafka:hello?brokers=127.0.0.1:8080&consumersCount=2&autoCommitEnable=false");
		Assert.assertNotNull(connector);
		Assert.assertTrue(connector instanceof KafkaConnector);
		connector.start();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {

		}

		connector.stop();
	}
}
