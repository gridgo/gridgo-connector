package io.gridgo.socket.netty4.codec;

import java.util.List;

import io.gridgo.bean.BElement;
import io.gridgo.utils.helper.Loggable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MsgpackDecoder extends ByteToMessageDecoder implements Loggable {

	public static final MsgpackDecoder DEFAULT = new MsgpackDecoder();

	private MsgpackDecoder() {
		// do nothing
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		try (ByteBufInputStream stream = new ByteBufInputStream(in)) {
			while (in.isReadable()) {
				in.markReaderIndex();
				out.add(BElement.fromRaw(new ByteBufInputStream(in)));
			}
		} catch (Exception e) {
			in.resetReaderIndex();
		}
	}
}
