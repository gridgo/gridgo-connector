package io.gridgo.connector.rabbitmq.impl;

import com.rabbitmq.client.Connection;

import io.gridgo.connector.rabbitmq.RabbitMQQueueConfig;

public class DefaultRabbitMQProducer extends AbstractRabbitMQProducer {

	public DefaultRabbitMQProducer(Connection connection, RabbitMQQueueConfig queueConfig) {
		super(connection, queueConfig);
	}
}
