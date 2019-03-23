package io.gridgo.socket.netty4.raw;

import io.gridgo.socket.netty4.raw.codec.BelementDecoder;
import io.gridgo.socket.netty4.raw.codec.BelementEncoder;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public final class Netty4RawChannelPreset {

    public static void applyLengthPrepender(SocketChannel socketChannel) {
        socketChannel.pipeline().addLast(//
                new LengthFieldPrepender(4), //
                new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
    }

    public static void applyBElementCodec(SocketChannel socketChannel, String format) {
        socketChannel.pipeline().addLast(//
                new BelementEncoder(format), //
                new BelementDecoder(format));
    }
}
