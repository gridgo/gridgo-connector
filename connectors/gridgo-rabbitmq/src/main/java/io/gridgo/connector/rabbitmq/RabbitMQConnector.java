package io.gridgo.connector.rabbitmq;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.DefaultCredentialsProvider;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.rabbitmq.impl.DefaultRabbitMQConsumer;
import io.gridgo.connector.rabbitmq.impl.DefaultRabbitMQProducer;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;
import io.gridgo.utils.support.HostAndPortSet;

@ConnectorEndpoint(scheme = "rabbitmq", syntax = "//{address}[/{exchangeName}]")
public class RabbitMQConnector extends AbstractConnector {

	private static final int DEFAULT_PORT = 5672;

	private List<Address> address;

	private final ConnectionFactory factory = new ConnectionFactory();

	private RabbitMQQueueConfig queueConfig;

	protected Connection newConnection() {
		try {
			return factory.newConnection(address);
		} catch (IOException | TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onInit() {
		ConnectorConfig config = this.getConnectorConfig();
		HostAndPortSet hostAndPortSet = new HostAndPortSet(config.getPlaceholders().getProperty("address"));
		if (hostAndPortSet.isEmpty()) {
			throw new InvalidPlaceholderException("Broker address(es) must be provided");
		}

		this.address = hostAndPortSet.convert((entry) -> {
			return new Address(entry.getHostOrDefault(LOCALHOST), entry.getPortOrDefault(DEFAULT_PORT));
		});

		String username = (String) config.getParameters().getOrDefault("username", "");
		if (!username.equals("")) {
			String password = (String) config.getParameters().getOrDefault("password", "");
			this.factory.setCredentialsProvider(new DefaultCredentialsProvider(username, password));
		}

		long autoRecoveryInterval = Long
				.parseLong((String) config.getParameters().getOrDefault("autoRecoveryInterval", "1000"));
		this.factory.setNetworkRecoveryInterval(autoRecoveryInterval);

		String exchangeName = (String) this.getConnectorConfig().getPlaceholders().getOrDefault("exchangeName", "");

		BObject configObject = BElement.fromAny(getConnectorConfig().getParameters());
		configObject.setAny("exchangeName", exchangeName);

		queueConfig = new RabbitMQQueueConfig(configObject);
	}

	@Override
	protected void onStart() {
		this.consumer = Optional.of(new DefaultRabbitMQConsumer(newConnection(), queueConfig));
		this.producer = Optional.of(new DefaultRabbitMQProducer(newConnection(), queueConfig));
	}

}
