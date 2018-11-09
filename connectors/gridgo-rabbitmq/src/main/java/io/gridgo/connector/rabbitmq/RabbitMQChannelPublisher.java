package io.gridgo.connector.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.DeliverCallback;

public interface RabbitMQChannelPublisher extends RabbitMQChannelLifeCycle {

	default void publish(byte[] body, BasicProperties props, String routingKey) {
		try {
			if (routingKey == null) {
				routingKey = this.getQueueConfig().getDefaultRoutingKey();
			}
			this.getChannel().basicPublish(this.getQueueConfig().getExchangeName(), routingKey, props, body);
		} catch (IOException e) {
			throw new RuntimeException("Publish message to channel error", e);
		}
	}

	default String initResponseQueue(DeliverCallback callback) {
		try {
			DeclareOk ok = this.getChannel().queueDeclare();
			final String responseQueueName = ok.getQueue();
			this.getChannel().basicConsume(responseQueueName, false, callback, (String consumerTag) -> {
				getLogger().debug("Response canceled: " + consumerTag);
			});
			return responseQueueName;
		} catch (IOException e) {
			throw new RuntimeException("Cannot declare response queue", e);
		}
	}
}
