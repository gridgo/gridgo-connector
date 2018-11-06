package io.gridgo.socket.nanomsg;

import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.socket.SocketConnector;

@ConnectorEndpoint(scheme = "nanomsg", syntax = "{type}:{transport}://{host}:{port}")
public class NNConnector extends SocketConnector {

	private static final NNSocketFactory FACTORY = new NNSocketFactory();

	public NNConnector() {
		super(FACTORY);
	}
}
