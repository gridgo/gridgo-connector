package io.gridgo.connector.netty4;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.support.HostAndPort;

@ConnectorEndpoint(scheme = "netty4", syntax = "{type}:{transport}://{host}[:{port}][/{path}]")
public class Netty4Connector extends AbstractConnector {

	public static final Collection<String> ALLOWED_TYPES = Arrays.asList("server", "client");

	private Netty4Transport transport;
	private HostAndPort host;
	private String path;
	private BObject options;
	private String type;

	@Override
	protected void onInit() {
		final ConnectorConfig config = getConnectorConfig();
		type = (String) config.getPlaceholders().getOrDefault("type", null);
		if (type == null || !ALLOWED_TYPES.contains(type.trim().toLowerCase())) {
			throw new InvalidPlaceholderException("type must be provided and is one of " + ALLOWED_TYPES);
		}

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

		this.host = HostAndPort.newInstance(hostStr, port);
		this.options = BObject.newDefault(config.getParameters());
		this.path = (String) config.getPlaceholders().getProperty("path", "websocket");

		initProducerAndConsumer();
	}

	private void initProducerAndConsumer() {
		switch (type) {
		case "server":
			Netty4Consumer consumer = Netty4Consumer.newDefault(this.getContext(), transport, host, path, options);
			consumer.start();
			this.consumer = Optional.of(consumer);
			this.producer = Optional.of(consumer.getResponder());
			break;
		case "client":
			Netty4Producer producer = Netty4Producer.newDefault(this.getContext(), transport, host, path, options);
			producer.start();
			this.producer = Optional.of(producer);
			this.consumer = Optional.of(producer.getReceiver());
			break;
		}
	}
}
