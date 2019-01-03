package io.gridgo.connector.rabbitmq.impl;

import com.rabbitmq.client.Connection;

import io.gridgo.connector.rabbitmq.RabbitMQQueueConfig;
import io.gridgo.connector.support.config.ConnectorContext;

public class DefaultRabbitMQConsumer extends AbstractRabbitMQConsumer {

    public DefaultRabbitMQConsumer(ConnectorContext context, Connection connection, RabbitMQQueueConfig queueConfig, String uniqueIdentifier) {
        super(context, connection, queueConfig, uniqueIdentifier);
    }
}
