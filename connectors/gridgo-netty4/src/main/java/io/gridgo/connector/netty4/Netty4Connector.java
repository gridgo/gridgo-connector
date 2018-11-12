package io.gridgo.connector.netty4;

import java.util.Optional;

import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.netty4.impl.DefaultNetty4Consumer;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.support.HostAndPort;

@ConnectorEndpoint(scheme = "netty4", syntax = "{transport}://{host}:{port}[/{path}]")
public class Netty4Connector extends AbstractConnector {

	private HostAndPort host;
	private Netty4Transport transport;
	private BObject options;

	@Override
	protected void onInit() {
		final ConnectorConfig config = getConnectorConfig();
		String transportName = (String) config.getPlaceholders().getOrDefault("transport", null);

		transport = Netty4Transport.fromName(transportName);
		if (transport == null) {
			throw new InvalidPlaceholderException(
					"transport must be provided and is one of " + Netty4Transport.values());
		}

		String hostStr = (String) config.getPlaceholders().getOrDefault("host", "localhost");
		if (hostStr == null) {
			throw new InvalidPlaceholderException("Host must be provided by ip, domain name or interface name");
		}

		int port = Integer.parseInt((String) config.getPlaceholders().getOrDefault("port", "0"));
		if (port <= 0) {
			throw new InvalidPlaceholderException("port must be provided and positive (port > 0)");
		}

		this.host = HostAndPort.newInstance(hostStr, port);

		this.options = BObject.newDefault(config.getParameters());
	}

	@Override
	protected void onStart() {
		this.consumer = Optional.of(new DefaultNetty4Consumer(transport, host, options));
		// this.producer = Optional.of(new DefaultNetty4Producer(transport, host,
		// options));
	}
}
