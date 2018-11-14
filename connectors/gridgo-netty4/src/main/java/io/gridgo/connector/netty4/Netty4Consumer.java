package io.gridgo.connector.netty4;

import io.gridgo.bean.BObject;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.HasResponder;
import io.gridgo.connector.netty4.impl.DefaultNetty4Consumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.support.HostAndPort;

public interface Netty4Consumer extends Consumer, HasResponder, FailureHandlerAware<Netty4Consumer> {

	static Netty4Consumer newDefault(ConnectorContext context, Netty4Transport transport, HostAndPort host, String path,
			BObject options) {
		return new DefaultNetty4Consumer(context, transport, host, path, options);
	}
}
