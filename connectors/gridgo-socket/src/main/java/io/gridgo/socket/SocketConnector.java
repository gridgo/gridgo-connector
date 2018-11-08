package io.gridgo.socket;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.framework.AbstractComponentLifecycle;
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
public class SocketConnector extends AbstractComponentLifecycle implements Connector {

	private final SocketFactory factory;

	private String type;
	private String address;
	private Map<String, Object> params;
	private Optional<Producer> producer = Optional.empty();
	private Optional<Consumer> consumer = Optional.empty();
	private CountDownLatch event = null;

	@Getter
	private ConnectorConfig connectorConfig;

	private Socket socket;

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

	@Override
	protected void onStart() {
		event = new CountDownLatch(1);
		CountDownLatch latch = new CountDownLatch(1);
		new Thread(() -> startSocket(latch)).start();
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
	}

	private void startSocket(CountDownLatch latch) {
		this.consumer = createConsumer();
		this.producer = createProducer();

		if (this.consumer.isPresent())
			this.consumer.get().start();
		if (this.producer.isPresent())
			this.producer.get().start();
		latch.countDown();
		try {
			this.event.await();
		} catch (InterruptedException e) {

		}
		if (this.producer.isPresent())
			this.producer.get().stop();
		if (this.consumer.isPresent())
			this.consumer.get().stop();

		if (this.socket != null)
			this.socket.close();
	}

	@Override
	protected void onStop() {
		this.event.countDown();
	}

	@Override
	public Optional<Producer> getProducer() {
		return producer;
	}

	@Override
	public Optional<Consumer> getConsumer() {
		return consumer;
	}

	private Optional<Producer> createProducer() {
		if (type.equalsIgnoreCase("push") || type.equalsIgnoreCase("pub")) {
			this.socket = initSocket();
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
		return Optional.empty();
	}

	private Optional<Consumer> createConsumer() {
		if (type.equalsIgnoreCase("pull") || type.equalsIgnoreCase("sub")) {
			this.socket = initSocket();
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
