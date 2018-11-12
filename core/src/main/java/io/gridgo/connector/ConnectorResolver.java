package io.gridgo.connector;

import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContext;
import lombok.NonNull;

public interface ConnectorResolver {

	public default Connector resolve(final @NonNull String endpoint) {
		return resolve(endpoint, new DefaultConnectorContext());
	}

	public Connector resolve(final @NonNull String endpoint, ConnectorContext connectorContext);
}
