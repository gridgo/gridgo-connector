package io.gridgo.socket;

import io.gridgo.connector.Consumer;
import io.gridgo.socket.impl.DefaultSocketConsumer;

public interface SocketConsumer extends Consumer {

	long getTotalRecvBytes();

	long getTotalRecvMessages();

	static SocketConsumer newDefault(Socket socket) {
		return new DefaultSocketConsumer(socket);
	}

	static SocketConsumer newDefault(Socket socket, int bufferSize) {
		return new DefaultSocketConsumer(socket, bufferSize);
	}
}
