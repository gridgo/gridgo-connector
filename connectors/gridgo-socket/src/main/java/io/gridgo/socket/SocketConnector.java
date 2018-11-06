package io.gridgo.socket;

import java.util.Map;
import java.util.Optional;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.config.ConnectorConfig;
import lombok.Getter;

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

		return null;
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
		Socket socket = initSocket();
		socket.connect(this.address);
		return Optional.of(SocketProducer.newDefault(socket));
	}

	@Override
	public Optional<Consumer> getConsumer() {
		Socket socket = initSocket();
		socket.bind(this.address);
		return Optional.of(SocketConsumer.newDefault(socket));
	}
}
