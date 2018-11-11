package io.gridgo.socket;

import io.gridgo.connector.Consumer;
import io.gridgo.socket.impl.DefaultSocketConsumer;

public interface SocketConsumer extends Consumer {

	public final int DEFAULT_RECV_TIMEOUT = 100; // receive timeout apply on polling event loop

	long getTotalRecvBytes();

	long getTotalRecvMessages();

	static SocketConsumer newDefault(SocketFactory factory, SocketOptions options, String address, int bufferSize) {
		return new DefaultSocketConsumer(factory, options, address, bufferSize);
	}

}
