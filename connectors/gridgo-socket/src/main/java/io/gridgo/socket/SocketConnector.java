package io.gridgo.socket;

import java.util.Map;
import java.util.Optional;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.config.ConnectorConfig;
import lombok.Getter;

/**
 * The sub-class must annotated by ConnectorResolver which syntax has at least 4
 * placeholders: {type} (push, pull, pub, sub) {transport} (tcp, pgm, epgm,
 * inproc, ipc), {host} (allow ipv4, ipv6 (with bracket []), hostname or
 * interface and {port}
 *
 * @author bachden
 *
 */
public class SocketConnector implements Connector {

	private final SocketFactory factory;

	private String type;
	private String address;
	private Map<String, Object> params;

	@Getter
	private ConnectorConfig connectorConfig;

	protected SocketConnector(SocketFactory factory) {
		this.factory = factory;
	}

	@Override
	public Connector initialize(ConnectorConfig config) {
		this.connectorConfig = config;
		String type = config.getPlaceholders().getProperty("type");
		String transport = config.getPlaceholders().getProperty("transport");
		String host = config.getPlaceholders().getProperty("host");
		int port = Integer.parseInt(config.getPlaceholders().getProperty("port"));

		this.address = transport + "://" + host + ":" + port;
		this.params = config.getParameters();
		this.type = type;

		return this;
	}

	private Socket initSocket() {
		SocketOptions options = new SocketOptions();
		options.setType(type);
		options.getConfig().putAll(this.params);
		Socket socket = factory.createSocket(options);
		return socket;
	}

	@Override
	public Optional<Producer> getProducer() {
		if (type.equalsIgnoreCase("push") || type.equalsIgnoreCase("pub")) {
			Socket socket = initSocket();
			switch (type) {
			case "push":
				socket.connect(address);
				break;
			case "pub":
				socket.bind(address);
				break;
			}
			return Optional.of(SocketProducer.newDefault(socket));
		}
		return Optional.ofNullable(null);
	}

	@Override
	public Optional<Consumer> getConsumer() {
		if (type.equalsIgnoreCase("pull") || type.equalsIgnoreCase("sub")) {
			Socket socket = initSocket();
			switch (type) {
			case "pull":
				socket.bind(this.address);
				break;
			case "sub":
				socket.connect(address);
				break;
			}
			return Optional.of(SocketConsumer.newDefault(socket));
		}
		return Optional.ofNullable(null);
	}
}
