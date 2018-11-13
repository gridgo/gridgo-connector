package io.gridgo.connector.netty4.impl;

import io.gridgo.bean.BObject;
import io.gridgo.connector.Receiver;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.support.HostAndPort;

public class DefaultNetty4Producer extends AbstractNetty4Producer {

	public DefaultNetty4Producer(ConnectorContext context, Netty4Transport transport, HostAndPort host,
			BObject options) {
		super(context, transport, host, options);
	}

	@Override
	protected Receiver createReceiver() {
		return new DefaultNetty4Receiver(getContext(), getSocketClient());
	}
}
