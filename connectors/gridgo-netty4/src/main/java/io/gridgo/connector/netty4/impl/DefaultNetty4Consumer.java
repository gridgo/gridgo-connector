package io.gridgo.connector.netty4.impl;

import io.gridgo.bean.BObject;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.support.HostAndPort;

public class DefaultNetty4Consumer extends AbstractNetty4Consumer {

	public DefaultNetty4Consumer(Netty4Transport transport, HostAndPort host, BObject options) {
		super(transport, host, options);
	}
}
