package io.gridgo.connector;

import lombok.NonNull;

public interface ConnectorFactory {

	public Connector createConnector(final @NonNull String endpoint);
	
	public Connector createConnector(final @NonNull String endpoint, final @NonNull ConnectorResolver resolver);
}
