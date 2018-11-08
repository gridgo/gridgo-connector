package io.gridgo.socket;

import java.util.Map;
import java.util.Optional;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.config.ConnectorConfig;

/**
 * The sub-class must annotated by ConnectorResolver which syntax has at least 4
 * placeholders: {type} (push, pull, pub, sub) {transport} (tcp, pgm, epgm,
 * inproc, ipc), {host} (allow ipv4, ipv6 (with bracket []), hostname or
 * interface and {port}
 *
 * @author bachden
 *
 */
public class SocketConnector extends AbstractConnector implements Connector {

	private final SocketFactory factory;

	private String type;
	private String address;
	private Map<String, Object> params;
	
	protected SocketConnector(SocketFactory factory) {
		this.factory = factory;
	}

	@Override
	public void onInit() {
		ConnectorConfig config = getConnectorConfig();
		String type = config.getPlaceholders().getProperty("type");
		String transport = config.getPlaceholders().getProperty("transport");
		String host = config.getPlaceholders().getProperty("host");
		int port = Integer.parseInt(config.getPlaceholders().getProperty("port"));

		this.address = transport + "://" + host + ":" + port;
		this.params = config.getParameters();
		this.type = type;
	}

	@Override
	protected void onStart() {
		this.consumer = createConsumer();
		this.producer = createProducer();

		if (this.consumer.isPresent())
			this.consumer.get().start();
		if (this.producer.isPresent())
			this.producer.get().start();
	}

	@Override
	protected void onStop() {
		if (this.producer.isPresent())
			this.producer.get().stop();
		if (this.consumer.isPresent())
			this.consumer.get().stop();
		
		this.consumer = Optional.empty();
		this.producer = Optional.empty();
	}

	private Optional<Producer> createProducer() {
		if (type.equalsIgnoreCase("push") || type.equalsIgnoreCase("pub")) {
			Socket socket = initSocket();
			return Optional.of(SocketProducer.newDefault(socket, type, address));
		}
		return Optional.empty();
	}

	private Optional<Consumer> createConsumer() {
		if (type.equalsIgnoreCase("pull") || type.equalsIgnoreCase("sub")) {
			Socket socket = initSocket();
			return Optional.of(SocketConsumer.newDefault(socket, type, address));
		}
		return Optional.empty();
	}

	private Socket initSocket() {
		SocketOptions options = new SocketOptions();
		options.setType(type);
		options.getConfig().putAll(this.params);
		Socket socket = factory.createSocket(options);
		return socket;
	}
}
