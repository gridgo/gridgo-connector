package io.gridgo.dummy.test;

import java.util.Optional;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.config.ConnectorContext;
import lombok.Getter;

@ConnectorEndpoint(scheme="dummy", syntax="{type}:{transport}://{host}:{port}")
public class DummyConnector implements Connector {
	
	@Getter
	private ConnectorConfig connectorConfig;

	@Getter
	private ConnectorContext context;
	
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
	public Connector initialize(ConnectorConfig config, ConnectorContext context) {
		this.context = context;
		this.connectorConfig = config;
		return this;
	}
}
