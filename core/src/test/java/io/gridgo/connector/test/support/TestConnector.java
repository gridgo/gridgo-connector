package io.gridgo.connector.test.support;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "test", syntax = "{type}:{transport}://{host}:{port}")
public class TestConnector extends AbstractConnector {

	protected void onInit() {
		this.consumer = Optional.of(new TestConsumer());
		this.producer = Optional.of(new TestProducer());
	}
}
