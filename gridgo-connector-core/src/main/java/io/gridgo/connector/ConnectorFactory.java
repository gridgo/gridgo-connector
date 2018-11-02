package io.gridgo.connector;

public interface ConnectorFactory {

	public default Connector createConnector(String endpoint) {
		return createConnector(endpoint, false);
	}
	
	public default Connector createConnector(String endpoint, ConnectorResolver resolver) {
		return createConnector(endpoint, resolver, false);
	}

	public Connector createConnector(String endpoint, boolean prototype);

	public Connector createConnector(String endpoint, ConnectorResolver resolver, boolean prototype);
	
	public ConnectorFactory setDefaultConnectorResolver(ConnectorResolver resolver);
}
