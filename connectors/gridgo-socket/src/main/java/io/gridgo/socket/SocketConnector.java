package io.gridgo.socket;

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

	private String address;
	private SocketOptions options;
	private final SocketFactory factory;

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

		this.options = new SocketOptions();
		this.options.setType(type);
		this.options.getConfig().putAll(config.getParameters());

		this.consumer = createConsumer();
		this.producer = createProducer();
	}

	@Override
	protected void onStart() {
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
	}

	private Optional<Producer> createProducer() {
		if (this.options.getType().equalsIgnoreCase("push") || this.options.getType().equalsIgnoreCase("pub")) {
			return Optional.of(SocketProducer.newDefault(this.factory, this.options, this.address));
		}
		return Optional.empty();
	}

	private Optional<Consumer> createConsumer() {
		if (this.options.getType().equalsIgnoreCase("pull") || this.options.getType().equalsIgnoreCase("sub")) {
			return Optional.of(SocketConsumer.newDefault(this.factory, this.options, this.address));
		}
		return Optional.empty();
	}
}
