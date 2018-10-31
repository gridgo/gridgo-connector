package io.gridgo.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQFactory {

	private final ConnectionFactory connectionFactory;

	public RabbitMQFactory(RabbitMQFactoryConfig config) {
		this.connectionFactory = new ConnectionFactory();
		this.connectionFactory.setAutomaticRecoveryEnabled(config.isAutomaticRecoveryEnabled());
		this.connectionFactory.setChannelRpcTimeout(config.getChannelRpcTimeout());
		this.connectionFactory.setChannelShouldCheckRpcResponseType(config.isChannelShouldCheckRpcResponseType());
	}
}
