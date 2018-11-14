package io.gridgo.socket.netty4.raw;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public abstract class AbstractNetty4RawSocketClient extends AbstractNetty4SocketClient {

	@Override
	protected void onInitChannel(SocketChannel socketChannel) {
		Netty4RawChannelPreset.applyLengthPrepender(socketChannel);
		Netty4RawChannelPreset.applyMsgpackCodec(socketChannel);
	}

	@Override
	protected Bootstrap createBootstrap() {
		return new Bootstrap().channel(NioSocketChannel.class);
	}

	@Override
	protected BElement handleIncomingMessage(Object msg) throws Exception {
		return (BElement) msg;
	}
}
