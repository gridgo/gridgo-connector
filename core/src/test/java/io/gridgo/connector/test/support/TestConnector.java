package io.gridgo.connector.test.support;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "test", syntax = "{type}:{transport}://{host}:{port}")
public class TestConnector extends AbstractConnector {

	@Override
	protected void onInit() {
		this.consumer = Optional.of(new TestConsumer());
		this.producer = Optional.of(new TestProducer());
	}
	
	@Override
	protected void onStart() {
		if (consumer.isPresent())
			consumer.get().start();
		if (producer.isPresent())
			producer.get().start();
	}

	@Override
	protected void onStop() {
		if (consumer.isPresent())
			consumer.get().stop();
		if (producer.isPresent())
			producer.get().stop();
	}
}
