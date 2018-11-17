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

	static final int DEFAULT_BUFFER_SIZE = 128 * 1024;

	static final int DEFAULT_RINGBUFFER_SIZE = 1024;

	static final int DEFAULT_MAX_BATCH_SIZE = 1000;

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

		this.batchingEnabled = Boolean.valueOf(
				config.getParameters().getOrDefault(SocketConstants.BATCHING_ENABLED, this.batchingEnabled).toString());

		this.maxBatchSize = Integer.valueOf(
				config.getParameters().getOrDefault(SocketConstants.MAX_BATCH_SIZE, this.maxBatchSize).toString());

		this.bufferSize = Integer
				.valueOf(config.getParameters().getOrDefault(SocketConstants.BUFFER_SIZE, this.bufferSize).toString());

		this.ringBufferSize = Integer.valueOf(
				config.getParameters().getOrDefault(SocketConstants.RING_BUFFER_SIZE, this.ringBufferSize).toString());

		this.options = new SocketOptions();
		this.options.setType(type);
		this.options.getConfig().putAll(config.getParameters());

		this.consumer = createConsumer();
		this.producer = createProducer();
	}

	private Optional<Producer> createProducer() {
		if (this.options.getType().equalsIgnoreCase(SocketConstants.TYPE_PUSH)
				|| this.options.getType().equalsIgnoreCase(SocketConstants.TYPE_PUBLISH)) {
			SocketProducer p = SocketProducer.newDefault(getContext(), factory, options, address, bufferSize,
					ringBufferSize, batchingEnabled, maxBatchSize);
			return Optional.of(p);
		}
		return Optional.empty();
	}

	private Optional<Consumer> createConsumer() {
		if (this.options.getType().equalsIgnoreCase(SocketConstants.TYPE_PULL)
				|| this.options.getType().equalsIgnoreCase(SocketConstants.TYPE_SUBSCRIBE)) {
			return Optional.of(SocketConsumer.newDefault(getContext(), factory, options, address, bufferSize));
		}
		return Optional.empty();
	}
}
