package io.gridgo.connector;

import lombok.NonNull;

public interface ConnectorResolver {

	public Connector resolve(final @NonNull String endpoint);
}
