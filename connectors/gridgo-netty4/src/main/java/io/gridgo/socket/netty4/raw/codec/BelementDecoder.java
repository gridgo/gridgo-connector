package io.gridgo.socket.netty4.raw.codec;

import java.util.List;

import io.gridgo.bean.BElement;
import io.gridgo.utils.helper.Loggable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BelementDecoder extends ByteToMessageDecoder implements Loggable {

    private final String format;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        ByteBufInputStream stream = new ByteBufInputStream(in);
        try {
            out.add(BElement.ofBytes(stream, format));
        } catch (Exception e) {
            in.resetReaderIndex();
            throw e;
        }
    }
}
