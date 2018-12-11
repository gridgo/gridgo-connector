package io.gridgo.connector;

import java.util.Optional;

import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.ComponentLifecycle;

public interface Connector extends ComponentLifecycle {

    public Connector initialize(ConnectorConfig config, ConnectorContext context);

    public Optional<Producer> getProducer();

    public Optional<Consumer> getConsumer();

    public ConnectorConfig getConnectorConfig();
}
