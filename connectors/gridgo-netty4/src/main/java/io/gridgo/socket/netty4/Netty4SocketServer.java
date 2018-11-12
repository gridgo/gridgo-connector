package io.gridgo.socket.netty4;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.gridgo.bean.BElement;
import io.gridgo.utils.support.HostAndPort;
import io.netty.channel.ChannelFuture;

public interface Netty4SocketServer extends Netty4Socket {

	void bind(HostAndPort host);

	ChannelFuture send(long routingId, BElement data);

	void setReceiveCallback(BiConsumer<Long, BElement> onReceiveCallback);

	void setChannelOpenCallback(Consumer<Long> onChannelOpenCallback);

	void setChannelCloseCallback(Consumer<Long> onChannelCloseCallback);
}
