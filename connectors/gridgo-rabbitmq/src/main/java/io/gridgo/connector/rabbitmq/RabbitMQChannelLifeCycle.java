package io.gridgo.connector.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.gridgo.utils.helper.Loggable;

public interface RabbitMQChannelLifeCycle extends Loggable {

	RabbitMQQueueConfig getQueueConfig();

	Channel getChannel();

	default Channel initChannel(Connection connection) {
		try {
			Channel channel = connection.createChannel();
			if (getQueueConfig().getExchangeName() != null && !getQueueConfig().getExchangeName().isBlank()) {
				channel.exchangeDeclare(getQueueConfig().getExchangeName(), getQueueConfig().getExchangeType());
			}

			channel.queueDeclare(getQueueConfig().getQueueName(), getQueueConfig().isDurable(),
					getQueueConfig().isExclusive(), getQueueConfig().isAutoDelete(), null);

			for (String routingKey : getQueueConfig().getRoutingKeys()) {
				channel.queueBind(getQueueConfig().getQueueName(), getQueueConfig().getExchangeName(), routingKey);
			}
			return channel;
		} catch (Exception e) {
			throw new RuntimeException("Init channel error", e);
		}
	}

	default void closeChannel() {
		try {
			getChannel().close();
		} catch (IOException | TimeoutException e) {
			throw new RuntimeException("Close channel error", e);
		}
	}
}
