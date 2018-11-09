package io.gridgo.connector.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.DeliverCallback;

public interface RabbitMQChannelSubscriber extends RabbitMQChannelLifeCycle {

	default void subscibe(DeliverCallback onMessageCallback, CancelCallback onCancelCallback) {
		try {
			this.getChannel().basicConsume(this.getQueueConfig().getQueueName(), false, onMessageCallback,
					onCancelCallback);
		} catch (IOException e) {
			throw new RuntimeException("Cannot init basic consume", e);
		}
	}
}
