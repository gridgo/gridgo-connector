package io.gridgo.connector.rabbitmq.impl;

import com.rabbitmq.client.Connection;

import io.gridgo.connector.rabbitmq.RabbitMQQueueConfig;
import io.gridgo.connector.support.config.ConnectorContext;

public class DefaultRabbitMQProducer extends AbstractRabbitMQProducer {

	public DefaultRabbitMQProducer(ConnectorContext context, Connection connection, RabbitMQQueueConfig queueConfig) {
		super(context, connection, queueConfig);
	}

	@Override
	protected String generateName() {
		// TODO Auto-generated method stub
		return null;
	}
}
