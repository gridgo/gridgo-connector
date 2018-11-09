package io.gridgo.connector.rabbitmq.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.rabbitmq.RabbitMQConnector;

public class TestRabbitMQ {

	private static final ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector");

	@Test
	public void testDirectQueue() {
		Connector connector = resolver.resolve("rabbimtq://localhost");

		assertNotNull(connector);
		assertTrue(connector instanceof RabbitMQConnector);
	}
}
