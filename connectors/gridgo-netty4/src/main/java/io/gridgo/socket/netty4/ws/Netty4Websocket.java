package io.gridgo.socket.netty4.ws;

import io.gridgo.socket.netty4.Netty4Socket;

public interface Netty4Websocket extends Netty4Socket {

    Netty4WebsocketFrameType getFrameType();

    void setPath(String path);
}
