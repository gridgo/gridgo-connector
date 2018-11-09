package io.gridgo.connector.rabbitmq.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;

import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.rabbitmq.RabbitMQChannelSubscriber;
import io.gridgo.connector.rabbitmq.RabbitMQConsumer;
import io.gridgo.connector.rabbitmq.RabbitMQQueueConfig;
import io.gridgo.framework.support.Message;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractRabbitMQConsumer extends AbstractConsumer
		implements RabbitMQConsumer, RabbitMQChannelSubscriber {

	private final Connection connection;

	@Getter
	private final RabbitMQQueueConfig queueConfig;

	@Getter
	private Channel channel;

	protected AbstractRabbitMQConsumer(@NonNull Connection connection, @NonNull RabbitMQQueueConfig queueConfig) {
		this.connection = connection;
		this.queueConfig = queueConfig;
	}

	@Override
	protected void onStart() {
		this.channel = this.initChannel(connection);
		this.subscibe(this::onDelivery, this::onCancel);
	}

	@Override
	protected void onStop() {
		this.closeChannel();
		this.channel = null;
	}

	protected void onCancel(String consumerTag) {

	}

	private void onDelivery(String consumerTag, Delivery message) {
		
	}

	protected abstract void onMessage(Message message);

}
