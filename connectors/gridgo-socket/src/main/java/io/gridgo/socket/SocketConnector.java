package io.gridgo.socket;

import java.util.Arrays;
import java.util.Map;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.AbstractCachedConnector;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;

/**
 * The sub-class must annotated by ConnectorResolver which syntax has at least 4
 * placeholders: {type} (push, pull, pub, sub) {transport} (tcp, pgm, epgm,
 * inproc, ipc), {host} (allow ipv4, ipv6 (with bracket []), hostname or
 * interface and {port}
 *
 * @author bachden
 *
 */
public class SocketConnector extends AbstractCachedConnector {

	private final SocketFactory factory;

	private String type;
	private String address;
	private Map<String, Object> params;

	protected SocketConnector(SocketFactory factory) {
		this.factory = factory;
	}

	@Override
	public void onInit() {
		String type = getConnectorConfig().getPlaceholders().getProperty("type");

		if (type == null || !Arrays.asList("pull", "push", "pub", "sub").contains(type.toLowerCase().trim())) {
			throw new InvalidPlaceholderException(
					"Placeholder type is required and support only 4 values: \"pull\", \"push\", \"pub\", \"sub\"");
		}
		type = type.trim().toLowerCase();

		String transport = getConnectorConfig().getPlaceholders().getProperty("transport");
		if (transport == null
				|| !Arrays.asList("tcp", "pgm", "epgm", "inproc", "ipc").contains(transport.toLowerCase().trim())) {
			throw new InvalidPlaceholderException(
					"Placeholder `transport` is required and support only 5 values: \"tcp\", \"pgm\", \"epgm\", \"inproc\", \"ipc\"");
		}
		transport = transport.trim().toLowerCase();

		String host = getConnectorConfig().getPlaceholders().getProperty("host");
		int port = Integer.parseInt(getConnectorConfig().getPlaceholders().getProperty("port"));

		this.address = transport + "://" + host + ":" + port;
		this.params = getConnectorConfig().getParameters();
		this.type = type;

		boolean cacheProducer = Boolean.valueOf((String) params.getOrDefault("cacheProducer", "false"));
		boolean cacheConsumer = Boolean.valueOf((String) params.getOrDefault("cacheConsumer", "false"));

		this.params.remove("cacheProducer");
		this.params.remove("cacheConsumer");

		switch (type) {
		case "pull":
			cacheConsumer = true;
			break;
		case "pub":
			cacheProducer = true;
			break;
		}

		this.setCacheProducer(cacheProducer);
		this.setCacheConsumer(cacheConsumer);
	}

	private Socket initSocket() {
		SocketOptions options = new SocketOptions();
		options.setType(type);
		options.getConfig().putAll(this.params);
		Socket socket = factory.createSocket(options);
		return socket;
	}

	@Override
	public Producer newProducer() {
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
			return SocketProducer.newDefault(socket);
		}
		return null;
	}

	@Override
	public Consumer newConsumer() {
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
			return SocketConsumer.newDefault(socket);
		}
		return null;
	}

	@Override
	protected void onStart() {
		// do nothing
	}

	@Override
	protected void onStop() {
		// do nothing
	}
}
