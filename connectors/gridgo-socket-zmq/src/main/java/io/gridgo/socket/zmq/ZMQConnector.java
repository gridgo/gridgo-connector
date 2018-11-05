package io.gridgo.socket.zmq;

import java.util.Optional;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;

@ConnectorEndpoint(scheme = "zmq", syntax = "{type}:{transport}://{host}:port")
public class ZMQConnector implements Connector {

	@Override
	public Connector initialize(ConnectorConfig config) {
		
		return null;
	}

	@Override
	public Optional<Producer> getProducer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Consumer> getConsumer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConnectorConfig getConnectorConfig() {
		// TODO Auto-generated method stub
		return null;
	}

}
