package io.gridgo.socket.netty4.codec;

import io.gridgo.bean.BElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgpackEncoder extends MessageToByteEncoder<BElement> {

	@Override
	public boolean acceptOutboundMessage(Object msg) throws Exception {
		return msg instanceof BElement;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, BElement in, ByteBuf out) throws Exception {
		in.writeBytes(new ByteBufOutputStream(out));
	}

}
