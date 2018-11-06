package io.gridgo.socket.zmq;

import java.util.Map;
import java.util.Optional;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.socket.SocketOptions;
import io.gridgo.socket.producer.DefaultSocketProducer;

@ConnectorEndpoint(scheme = "zmq", syntax = "{type}:{transport}://{host}:{port}")
public class ZMQConnector implements Connector {

	private static final ZMQSocketFactory FACTORY = new ZMQSocketFactory(1);

	private String type;
	private String address;
	private Map<String, Object> params;

	@Override
	public Connector initialize(ConnectorConfig config) {
		String type = config.getPlaceholders().getProperty("type");
		String transport = config.getPlaceholders().getProperty("transport");
		String host = config.getPlaceholders().getProperty("host");
		int port = Integer.parseInt(config.getPlaceholders().getProperty("port"));

		this.address = transport + "://" + host + ":" + port;
		this.params = config.getParameters();
		this.type = type;

		return null;
	}

	@Override
	public Optional<Producer> getProducer() {
		SocketOptions options = new SocketOptions();
		options.setType(type);
		options.getConfig().putAll(this.params);
		ZMQSocket socket = FACTORY.createSocket(options);
		socket.connect(this.address);
		return Optional.of(new DefaultSocketProducer(socket));
	}

	@Override
	public Optional<Consumer> getConsumer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConnectorConfig getConnectorConfig() {
		// TODO Auto-generated method stub
		return null;
	}

}
