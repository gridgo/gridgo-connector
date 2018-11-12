package io.gridgo.socket.netty4.impl;

import io.gridgo.socket.netty4.Netty4SocketClient;
import lombok.NonNull;

public abstract class AbstractNetty4SocketClient extends AbstractNetty4Socket implements Netty4SocketClient {

	@Override
	public void connect(@NonNull final String address) {
		this.tryStart(() -> {

		});
	}
}
