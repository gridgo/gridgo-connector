package io.gridgo.connector.impl.factories;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorFactory;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import lombok.NonNull;

public class DefaultConnectorFactory implements ConnectorFactory {
	
	private static final ConnectorResolver DEFAULT_CONNECTOR_RESOLVER = new ClasspathConnectorResolver();
	
	private final ConnectorResolver resolver;

	public DefaultConnectorFactory() {
		this.resolver = DEFAULT_CONNECTOR_RESOLVER;
	}

	public DefaultConnectorFactory(final @NonNull ConnectorResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public Connector createConnector(String endpoint) {
		return resolver.resolve(endpoint);
	}

	@Override
	public Connector createConnector(String endpoint, ConnectorResolver resolver) {
		return resolver.resolve(endpoint);
	}
}
