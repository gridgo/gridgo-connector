package io.gridgo.socket;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.HasResponder;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.socket.impl.DefaultSocketConsumer;

public interface SocketConsumer extends Consumer, HasResponder {

    public final int DEFAULT_RECV_TIMEOUT = 100; // receive timeout apply on polling event loop

    static SocketConsumer of(ConnectorContext context, SocketFactory factory, SocketOptions options, String address, int bufferSize) {
        return new DefaultSocketConsumer(context, factory, options, address, bufferSize);
    }

    long getTotalRecvBytes();

    long getTotalRecvMessages();

}
