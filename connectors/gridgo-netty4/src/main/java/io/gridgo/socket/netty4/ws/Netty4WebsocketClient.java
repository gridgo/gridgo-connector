package io.gridgo.socket.netty4.ws;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.SocketChannel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class Netty4WebsocketClient extends AbstractNetty4SocketClient implements Netty4Websocket {

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private String path;

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private String proxy;

	@Override
	protected void onInitChannel(SocketChannel ch) {

	}

	@Override
	protected Bootstrap createBootstrap() {
		return null;
	}

	@Override
	protected BElement handleIncomingMessage(Object msg) throws Exception {
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
