package io.gridgo.connector;

import java.util.Optional;

import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.framework.ComponentLifecycle;

public interface Connector extends ComponentLifecycle {
	
	public Connector initialize(ConnectorConfig config);

	public Optional<Producer> getProducer();

	public Optional<Consumer> getConsumer();
	
	public ConnectorConfig getConnectorConfig();
}
