package io.gridgo.socket.zmq;

import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.socket.SocketConnector;

@ConnectorEndpoint(scheme = "zmq", syntax = "{type}:{transport}://{host}:{port}")
public class ZMQConnector extends SocketConnector {

	private static final ZMQSocketFactory FACTORY = new ZMQSocketFactory(1);

	public ZMQConnector() {
		super(FACTORY);
	}
}
