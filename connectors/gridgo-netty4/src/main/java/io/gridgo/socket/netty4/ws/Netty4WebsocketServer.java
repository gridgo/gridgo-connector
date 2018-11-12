package io.gridgo.socket.netty4.ws;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.SocketChannel;

public class Netty4WebsocketServer extends AbstractNetty4SocketServer {

	@Override
	protected BElement parseReceivedData(Object msg) throws Exception {
		// TODO implement parse received data websocket server
		return null;
	}

	@Override
	protected void onInitChannel(SocketChannel socketChannel) {
		// TODO implement channel initializing websocket server
	}

	@Override
	protected ServerBootstrap createBootstrap() {
		// TODO implement create boooststrap for websocket server
		return null;
	}
}
