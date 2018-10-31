package io.gridgo.connector;

public interface ConnectorResolver {

	Connector resolve(String endpoint);
}
