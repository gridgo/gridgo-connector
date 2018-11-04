package io.gridgo.connector.test;

import java.util.Optional;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;
import lombok.Getter;

@ConnectorEndpoint(scheme="test", syntax="test:name")
public class TestConnector implements Connector {
	
	@Getter
	private ConnectorConfig connectorConfig;
	
	@Override
	public void start() {
		
	}
	
	@Override
	public void stop() {
		
	}

	@Override
	public Optional<Producer> getProducer() {
		return Optional.empty();
	}

	@Override
	public Optional<Consumer> getConsumer() {
		return Optional.empty();
	}

	@Override
	public Connector initialize(ConnectorConfig config) {
		this.connectorConfig = config;
		return this;
	}
}
