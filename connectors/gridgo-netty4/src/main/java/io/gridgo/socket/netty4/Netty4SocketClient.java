package io.gridgo.socket.netty4;

import java.util.function.Consumer;

import io.gridgo.bean.BElement;
import io.gridgo.utils.support.HostAndPort;
import io.netty.channel.ChannelFuture;

public interface Netty4SocketClient extends Netty4Socket {

    void connect(HostAndPort host);

    ChannelFuture send(BElement data);

    void setReceiveCallback(Consumer<BElement> onReceiveCallback);

    void setChannelOpenCallback(Runnable onChannelOpenCallback);

    void setChannelCloseCallback(Runnable onChannelCloseCallback);
}
