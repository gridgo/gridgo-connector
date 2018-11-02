package io.gridgo.connector;

public interface ConnectorResolver {

	public Connector resolve(String endpoint);
}
