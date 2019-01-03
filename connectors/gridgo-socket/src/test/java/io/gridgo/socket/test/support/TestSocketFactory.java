package io.gridgo.socket.test.support;

import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketFactory;
import io.gridgo.socket.SocketOptions;

public class TestSocketFactory implements SocketFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Socket> T createSocket(SocketOptions options) {
        return (T) new TestSocket();
    }

    @Override
    public String getType() {
        return "test";
    }
}