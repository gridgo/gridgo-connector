package io.gridgo.socket.netty4.raw;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;

public abstract class AbstractNetty4RawSocketServer extends AbstractNetty4SocketServer {

    @Override
    protected void onInitChannel(SocketChannel socketChannel) {
        Netty4RawChannelPreset.applyLengthPrepender(socketChannel);
        Netty4RawChannelPreset.applyMsgpackCodec(socketChannel);
    }

    @Override
    protected BElement handleIncomingMessage(String channelId, Object msg) throws Exception {
        return (BElement) msg;
    }

    @Override
    public final ChannelFuture send(String routingId, BElement data) {
        Channel channel = this.getChannel(routingId);
        if (channel != null) {
            if (data == null) {
                channel.close();
            } else {
                return channel.writeAndFlush(data).addListener(ChannelFutureListener.CLOSE);
            }
        }
        return null;
    }
}
