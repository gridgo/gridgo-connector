package io.gridgo.connector.impl.factories;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorFactory;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.ConnectorContextBuilder;
import io.gridgo.connector.support.config.impl.DefautConnectorContextBuilder;
import lombok.NonNull;

public class DefaultConnectorFactory implements ConnectorFactory {

	private static final ConnectorResolver DEFAULT_CONNECTOR_RESOLVER = new ClasspathConnectorResolver();

	private static final ConnectorContextBuilder DEFAULT_CONNECTOR_CONTEXT_BUILDER = new DefautConnectorContextBuilder();

	private final ConnectorResolver resolver;

	private final ConnectorContextBuilder builder;

	public DefaultConnectorFactory() {
		this.resolver = DEFAULT_CONNECTOR_RESOLVER;
		this.builder = DEFAULT_CONNECTOR_CONTEXT_BUILDER;
	}

	public DefaultConnectorFactory(final @NonNull ConnectorResolver resolver) {
		this.resolver = resolver;
		this.builder = DEFAULT_CONNECTOR_CONTEXT_BUILDER;
	}

	public DefaultConnectorFactory(final @NonNull ConnectorResolver resolver, ConnectorContextBuilder builder) {
		this.resolver = resolver;
		this.builder = builder;
	}

	@Override
	public Connector createConnector(String endpoint) {
		return createConnector(endpoint, resolver);
	}

	@Override
	public Connector createConnector(String endpoint, ConnectorResolver resolver) {
		return createConnector(endpoint, resolver, builder.build());
	}

	@Override
	public Connector createConnector(String endpoint, ConnectorContext context) {
		return createConnector(endpoint, resolver, builder.build());
	}

	@Override
	public Connector createConnector(String endpoint, ConnectorResolver resolver, ConnectorContext context) {
		return resolver.resolve(endpoint, context);
	}
}
