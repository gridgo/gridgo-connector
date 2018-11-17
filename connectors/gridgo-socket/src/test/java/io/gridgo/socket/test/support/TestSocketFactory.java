package io.gridgo.socket.test.support;

import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketFactory;
import io.gridgo.socket.SocketOptions;

public class TestSocketFactory implements SocketFactory {

	@Override
	public String getType() {
		return "test";
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Socket> T createSocket(SocketOptions options) {
		return (T) new TestSocket();
	}
}