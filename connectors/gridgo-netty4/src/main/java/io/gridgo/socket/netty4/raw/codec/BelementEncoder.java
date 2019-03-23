package io.gridgo.socket.netty4.raw.codec;

import io.gridgo.bean.BElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BelementEncoder extends MessageToByteEncoder<BElement> {

    private final String format;

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return msg instanceof BElement;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, BElement in, ByteBuf out) throws Exception {
        in.writeBytes(new ByteBufOutputStream(out), format);
    }

}
