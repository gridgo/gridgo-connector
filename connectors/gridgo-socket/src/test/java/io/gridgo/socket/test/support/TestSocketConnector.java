package io.gridgo.socket.test.support;

import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.socket.SocketConnector;

@ConnectorEndpoint(scheme = "testsocket", syntax = "{type}:{transport}://{host}[:{port}]")
public class TestSocketConnector extends SocketConnector {

    public TestSocketConnector() {
        super(new TestSocketFactory());
    }
}
