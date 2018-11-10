package io.gridgo.socket;

import io.gridgo.connector.Consumer;
import io.gridgo.socket.impl.DefaultSocketConsumer;

public interface SocketConsumer extends Consumer {

	long getTotalRecvBytes();

	long getTotalRecvMessages();

	static SocketConsumer newDefault(SocketFactory factory, SocketOptions options, String address, int bufferSize) {
		return new DefaultSocketConsumer(factory, options, address, bufferSize);
	}

}
