package io.gridgo.connector.rabbitmq.impl;

import java.io.IOException;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.rabbitmq.RabbitMQConsumer;
import io.gridgo.connector.rabbitmq.RabbitMQQueueConfig;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.MessageParser;
import io.gridgo.framework.support.Payload;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractRabbitMQConsumer extends AbstractConsumer implements RabbitMQConsumer {

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

	private void onCancel(String consumerTag) {
		getLogger().info("Cancelled " + consumerTag);
	}

	private void response(Exception ex, BasicProperties props) {
		BObject headers = BObject.newDefault();
		headers.setAny("status", 500);
		BValue body = BValue
				.newDefault("Internal server error: " + ex.getMessage() == null ? "unknown message" : ex.getMessage());
		this.response(createMessage(headers, body), props);
	}

	private void response(Message response, BasicProperties props) {
		Payload payload = response.getPayload();
		byte[] body = BArray.newFromSequence(payload.getId().get(), payload.getHeaders(), payload.getBody()).toBytes();
		try {
			String responseQueue = props.getReplyTo();
			this.channel.basicPublish("", responseQueue, props, body);
		} catch (IOException e) {
			getLogger().error("Cannot send response to caller: " + response);
		}
	}

	protected Deferred<Message, Exception> createDeferred() {
		return new CompletableDeferredObject<>();
	}

	private void onDelivery(String consumerTag, Delivery delivery) {
		Message message = MessageParser.DEFAULT.parse(delivery.getBody());
		BasicProperties props = delivery.getProperties();
		if (props != null && props.getCorrelationId() != null) {
			Deferred<Message, Exception> deferred = createDeferred();
			deferred.promise().done((response) -> {
				response(message, props);
				if (!getQueueConfig().isAutoAck()) {
					this.sendAck(delivery.getEnvelope().getDeliveryTag());
				}
			}).fail((exception) -> {
				response(exception, props);
			});
			this.publish(message, deferred);
		} else {
			this.publish(message, null);
		}
	}

	private void sendAck(long deliveryTag) {
		try {
			this.getChannel().basicAck(deliveryTag, false);
		} catch (IOException e) {
			throw new RuntimeException("Cannot send ack for delivery tag: " + deliveryTag, e);
		}
	}
}
