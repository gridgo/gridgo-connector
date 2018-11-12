package io.gridgo.connector;

import io.gridgo.connector.support.config.ConnectorContext;
import lombok.NonNull;

public interface ConnectorFactory {

	public Connector createConnector(final @NonNull String endpoint);

	public Connector createConnector(final @NonNull String endpoint, final @NonNull ConnectorContext context);

	public Connector createConnector(final @NonNull String endpoint, final @NonNull ConnectorResolver resolver);

	public Connector createConnector(final @NonNull String endpoint, final @NonNull ConnectorResolver resolver,
			final @NonNull ConnectorContext context);
}
