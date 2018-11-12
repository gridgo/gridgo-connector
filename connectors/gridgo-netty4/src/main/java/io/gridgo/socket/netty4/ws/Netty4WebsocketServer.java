package io.gridgo.socket.netty4.ws;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketServer;

public class Netty4WebsocketServer extends AbstractNetty4SocketServer {

	@Override
	protected BElement parseReceivedData(Object msg) throws Exception {
		return null;
	}
}
