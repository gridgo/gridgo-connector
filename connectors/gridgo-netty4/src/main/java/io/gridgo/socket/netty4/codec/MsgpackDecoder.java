package io.gridgo.socket.netty4.codec;

import java.util.List;

import org.msgpack.core.MessageInsufficientBufferException;

import io.gridgo.bean.BElement;
import io.gridgo.utils.helper.Loggable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MsgpackDecoder extends ByteToMessageDecoder implements Loggable {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		in.markReaderIndex();
		ByteBufInputStream stream = new ByteBufInputStream(in);
		try {
			out.add(BElement.fromRaw(stream));
		} catch (Exception e) {
			log.error("Exception caught while decoding", e);
			if (e.getCause() instanceof MessageInsufficientBufferException) {
				in.resetReaderIndex();
			}
		}
	}
}
