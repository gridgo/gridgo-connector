package io.gridgo.socket;

import io.gridgo.connector.Consumer;
import io.gridgo.socket.impl.DefaultSocketConsumer;

public interface SocketConsumer extends Consumer {

	long getTotalRecvBytes();

	long getTotalRecvMessages();

	static SocketConsumer newDefault(Socket socket, String type, String address) {
		return new DefaultSocketConsumer(socket, type, address);
	}

	static SocketConsumer newDefault(Socket socket, String type, String address, int bufferSize) {
		return new DefaultSocketConsumer(socket, type, address, bufferSize);
	}
}
