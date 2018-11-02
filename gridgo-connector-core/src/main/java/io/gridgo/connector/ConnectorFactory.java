package io.gridgo.connector;

public class ConnectorFactory {

	public void register(String prefix, ConnectorResolver resolver) {

	}

	public void deregister(String prefix) {

	}

	public void isRegistered(String prefix) {

	}

	public void getResolver(String prefix) {

	}

	public Connector createConnector(String endpoint) {
		return null;
	}
}
