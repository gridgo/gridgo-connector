package io.gridgo.connector.nanomsg;

import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.socket.SocketConnector;
import io.gridgo.socket.nanomsg.NNSocketFactory;

@ConnectorEndpoint(scheme = "nanomsg", syntax = "{type}:{transport}:[{role}:]//[{interface};]{host}:[{port}]")
public class NNConnector extends SocketConnector {

    private static final NNSocketFactory FACTORY = new NNSocketFactory();

    public NNConnector() {
        super(FACTORY);
    }
}
