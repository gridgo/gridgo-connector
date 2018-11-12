package io.gridgo.connector.netty4;

import io.gridgo.bean.BObject;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.HasResponder;
import io.gridgo.connector.netty4.impl.DefaultNetty4Consumer;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.support.HostAndPort;

public interface Netty4Consumer extends Consumer, HasResponder {

	static Netty4Consumer newDefault(Netty4Transport transport, HostAndPort host, BObject options) {
		return new DefaultNetty4Consumer(transport, host, options);
	}
}
