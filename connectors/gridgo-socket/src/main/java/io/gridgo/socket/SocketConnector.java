package io.gridgo.socket;

import java.util.Optional;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;
import io.gridgo.connector.support.exceptions.MalformedEndpointException;

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

	public static final int DEFAULT_BUFFER_SIZE = 128 * 1024;

	public static final int DEFAULT_RINGBUFFER_SIZE = 1024;

	public static final int DEFAULT_MAX_BATCH_SIZE = 1000;

	private String address;
	private SocketOptions options;
	private final SocketFactory factory;

	private boolean batchingEnabled = false;
	private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;

	private int bufferSize = DEFAULT_BUFFER_SIZE;
	private int ringBufferSize = DEFAULT_RINGBUFFER_SIZE;

	protected SocketConnector(SocketFactory factory) {
		this.factory = factory;
	}

	@Override
	public void onInit() {
		ConnectorConfig config = getConnectorConfig();
		String type = config.getPlaceholders().getProperty(SocketConstants.TYPE);
		String transport = config.getPlaceholders().getProperty(SocketConstants.TRANSPORT);
		String host = config.getPlaceholders().getProperty(SocketConstants.HOST);
		int port = Integer.parseInt(config.getPlaceholders().getProperty(SocketConstants.PORT));

		this.address = transport + "://" + host + ":" + port;

		this.batchingEnabled = Boolean
				.valueOf(config.getParameters().getOrDefault("batchingEnabled", this.batchingEnabled).toString());

		this.maxBatchSize = Integer
				.valueOf(config.getParameters().getOrDefault("maxBatchSize", this.maxBatchSize).toString());

		this.bufferSize = Integer
				.valueOf(config.getParameters().getOrDefault("bufferSize", this.bufferSize).toString());

		this.ringBufferSize = Integer
				.valueOf(config.getParameters().getOrDefault("ringBufferSize", this.ringBufferSize).toString());

		this.options = new SocketOptions();
		this.options.setType(type);
		this.options.getConfig().putAll(config.getParameters());

		this.consumer = createConsumer();
		this.producer = createProducer();

		this.initConsumerAndProducer();
	}

	private void initConsumerAndProducer() {
		SocketProducer p = null;
		SocketConsumer c = null;
		switch (this.options.getType().toLowerCase()) {
		case "push":
		case "pub":
			p = SocketProducer.newDefault(getContext(), factory, options, address, bufferSize, ringBufferSize,
					batchingEnabled, maxBatchSize);
			break;
		case "pull":
		case "sub":
			c = SocketConsumer.newDefault(getContext(), factory, options, address, bufferSize);
			break;
		case "pair":
			String role = this.getPlaceholder("role");
			if (role == null || role.isBlank()) {
				throw new MalformedEndpointException("Pair socket require socket role (connect or bind)");
			}
			switch (role.trim().toLowerCase()) {
			case "connect":
				p = SocketProducer.newDefault(getContext(), factory, options, address, bufferSize, ringBufferSize,
						batchingEnabled, maxBatchSize);
				c = (SocketConsumer) p.getReceiver();
				break;
			case "bind":
				c = SocketConsumer.newDefault(getContext(), factory, options, address, bufferSize);
				p = (SocketProducer) c.getResponder();
				break;
			default:
				throw new InvalidPlaceholderException("Invalid pair socket role, expected 'connect' or 'bind'");
			}
			break;
		}
		this.producer = Optional.ofNullable(p);
		this.consumer = Optional.ofNullable(c);
	}

	private Optional<Producer> createProducer() {
		if (this.options.getType().equalsIgnoreCase("push") || this.options.getType().equalsIgnoreCase("pub")) {
			SocketProducer p = SocketProducer.newDefault(getContext(), factory, options, address, bufferSize,
					ringBufferSize, batchingEnabled, maxBatchSize);
			return Optional.of(p);
		}
		return Optional.empty();
	}

	private Optional<Consumer> createConsumer() {
		if (this.options.getType().equalsIgnoreCase("pull") || this.options.getType().equalsIgnoreCase("sub")) {
			return Optional.of(SocketConsumer.newDefault(getContext(), factory, options, address, bufferSize));
		}
		return Optional.empty();
	}
}
