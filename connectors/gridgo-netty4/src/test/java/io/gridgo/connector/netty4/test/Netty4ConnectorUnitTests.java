package io.gridgo.connector.netty4.test;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;

public class Netty4ConnectorUnitTests {

	public static void main(String[] args) {
		ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector");

		Connector connector = resolver.resolve("netty4:tcp://localhost:8888?lengthPrepend=false");
		connector.start();

		Consumer consumer = connector.getConsumer().get();
		consumer.start();

		consumer.subscribe((msg) -> {
			System.out.println("Got message: " + msg.getPayload().getBody());
		});
	}
}
