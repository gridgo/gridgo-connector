package io.gridgo.connector.rabbitmq.impl;

import com.rabbitmq.client.Connection;

import io.gridgo.connector.rabbitmq.RabbitMQQueueConfig;

public class DefaultRabbitMQConsumer extends AbstractRabbitMQConsumer {

	public DefaultRabbitMQConsumer(Connection connection, RabbitMQQueueConfig queueConfig) {
		super(connection, queueConfig);
	}

}
