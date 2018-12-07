package io.gridgo.socket.netty4;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.gridgo.bean.BElement;
import io.gridgo.utils.support.HostAndPort;
import io.netty.channel.ChannelFuture;

public interface Netty4SocketServer extends Netty4Socket {

	void bind(HostAndPort host);

	ChannelFuture send(String channelId, BElement data);

	void setReceiveCallback(BiConsumer<String, BElement> onReceiveCallback);

	void setChannelOpenCallback(Consumer<String> onChannelOpenCallback);

	void setChannelCloseCallback(Consumer<String> onChannelCloseCallback);

	Map<String, Object> getChannelDetails(String channelId);
}
