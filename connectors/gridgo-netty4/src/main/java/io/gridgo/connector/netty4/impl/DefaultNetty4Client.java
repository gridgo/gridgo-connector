package io.gridgo.connector.netty4.impl;

import io.gridgo.bean.BObject;
import io.gridgo.connector.Receiver;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.support.HostAndPort;

public class DefaultNetty4Client extends AbstractNetty4Client {

    public DefaultNetty4Client(ConnectorContext context, Netty4Transport transport, HostAndPort host, String path, BObject options) {
        super(context, transport, host, path, options);
    }

    @Override
    protected Receiver createReceiver() {
        return new DefaultNetty4Receiver(getContext(), getSocketClient(), this.getUniqueIdentifier());
    }
}
