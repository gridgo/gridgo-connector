package io.gridgo.connector;

import lombok.NonNull;

public interface ConnectorFactory {

	public default Connector createConnector(final @NonNull String endpoint) {
		return createConnector(endpoint, false);
	}
	
	public default Connector createConnector(final @NonNull String endpoint, final @NonNull ConnectorResolver resolver) {
		return createConnector(endpoint, resolver, false);
	}

	public Connector createConnector(final @NonNull String endpoint, boolean prototype);

	public Connector createConnector(final @NonNull String endpoint, final @NonNull ConnectorResolver resolver, boolean prototype);
	
	public ConnectorFactory setDefaultConnectorResolver(final @NonNull ConnectorResolver resolver);
}
