package io.gridgo.connector.zmq;

import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.socket.SocketConnector;
import io.gridgo.socket.zmq.ZMQSocketFactory;

@ConnectorEndpoint(scheme = "zmq", syntax = "{type}:{transport}:[{role}:]//[{interface};]{host}[:{port}]")
public class ZMQConnector extends SocketConnector {

    private static final ZMQSocketFactory FACTORY = new ZMQSocketFactory(1);

    public ZMQConnector() {
        super(FACTORY);
    }
}
