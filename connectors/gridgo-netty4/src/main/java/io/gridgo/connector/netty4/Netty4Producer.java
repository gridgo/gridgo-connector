package io.gridgo.connector.netty4;

import io.gridgo.bean.BObject;
import io.gridgo.connector.HasReceiver;
import io.gridgo.connector.Producer;
import io.gridgo.connector.netty4.impl.DefaultNetty4Producer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.support.HostAndPort;

public interface Netty4Producer extends Producer, HasReceiver {

	static Netty4Producer newDefault(ConnectorContext context, Netty4Transport transport, HostAndPort host, String path,
			BObject options) {
		return new DefaultNetty4Producer(context, transport, host, path, options);
	}
}
