package io.gridgo.socket.netty4.raw;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketServer;
import io.netty.channel.socket.SocketChannel;

public abstract class AbstractNetty4RawSocketServer extends AbstractNetty4SocketServer {

	@Override
	protected void onInitChannel(SocketChannel socketChannel) {
		Netty4RawSocketPreset.applyLengthPrepender(socketChannel);
		Netty4RawSocketPreset.applyMsgpackCodec(socketChannel);
	}

	@Override
	protected BElement handleIncomingMessage(long channelId, Object msg) throws Exception {
		return (BElement) msg;
	}
}
