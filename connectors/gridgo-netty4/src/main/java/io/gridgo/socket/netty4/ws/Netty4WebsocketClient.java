package io.gridgo.socket.netty4.ws;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.SocketChannel;

public class Netty4WebsocketClient extends AbstractNetty4SocketClient {

	@Override
	protected void onInitChannel(SocketChannel ch) {

	}

	@Override
	protected Bootstrap createBootstrap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected BElement parseReceivedData(Object msg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
