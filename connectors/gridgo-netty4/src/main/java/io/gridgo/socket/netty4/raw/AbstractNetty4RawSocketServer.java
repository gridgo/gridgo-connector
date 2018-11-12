package io.gridgo.socket.netty4.raw;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.codec.MsgpackDecoder;
import io.gridgo.socket.netty4.codec.MsgpackEncoder;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketServer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public abstract class AbstractNetty4RawSocketServer extends AbstractNetty4SocketServer {

	protected final void applyMsgpackCodec(SocketChannel socketChannel) {
		socketChannel.pipeline().addLast("msgpackEncoder", MsgpackEncoder.DEFAULT);
		socketChannel.pipeline().addLast("msgpackDecoder", MsgpackDecoder.DEFAULT);
	}

	protected final void applyLengthPrepender(SocketChannel socketChannel) {
		socketChannel.pipeline().addLast("lengthFrameEncoder", new LengthFieldPrepender(4));
		socketChannel.pipeline().addLast("lengthFrameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
	}

	@Override
	protected void onInitChannel(SocketChannel socketChannel) {
		if (this.getConfigs().getBoolean("lengthPrepend", true)) {
			this.applyLengthPrepender(socketChannel);
		}
		this.applyMsgpackCodec(socketChannel);
	}

	@Override
	protected BElement parseReceivedData(Object msg) throws Exception {
		return (BElement) msg;
	}
}
