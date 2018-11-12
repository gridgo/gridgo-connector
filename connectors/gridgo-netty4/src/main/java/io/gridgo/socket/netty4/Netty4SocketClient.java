package io.gridgo.socket.netty4;

import io.gridgo.bean.BElement;

public interface Netty4SocketClient extends Netty4Socket {

	boolean isConnected();

	void connect(String address);

	void send(BElement data);
}
