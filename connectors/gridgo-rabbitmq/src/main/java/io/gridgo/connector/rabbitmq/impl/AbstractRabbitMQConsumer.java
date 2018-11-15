package io.gridgo.connector.rabbitmq.impl;

import java.io.IOException;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.impl.CompletableDeferredObject;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.rabbitmq.RabbitMQConsumer;
import io.gridgo.connector.rabbitmq.RabbitMQQueueConfig;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractRabbitMQConsumer extends AbstractConsumer implements RabbitMQConsumer {

	private final Connection connection;

	@Getter
	private final RabbitMQQueueConfig queueConfig;

	@Getter
	private Channel channel;

	protected AbstractRabbitMQConsumer(ConnectorContext context, @NonNull Connection connection,
			@NonNull RabbitMQQueueConfig queueConfig) {
		super(context);
		this.connection = connection;
		this.queueConfig = queueConfig;
	}

	@Override
	protected String generateName() {
		return null;
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

	private void sendResponse(Exception ex, BasicProperties props) {
		BObject headers = BObject.newDefault();
		headers.setAny("status", 500);
		BValue body = BValue
				.newDefault("Internal server error: " + ex.getMessage() == null ? "unknown message" : ex.getMessage());
		this.sendResponse(createMessage(headers, body), props);
	}

	private void sendResponse(Message response, BasicProperties props) {
		final Payload payload = response.getPayload();

		final BValue id = payload.getId().orElse(null);
		final BObject headers = payload.getHeaders();
		final BElement body = payload.getBody();

		final String responseQueue = props.getReplyTo();
		final byte[] bytes = BArray.newFromSequence(id, headers, body).toBytes();

		try {
			this.getChannel().basicPublish("", responseQueue, props, bytes);
		} catch (IOException e) {
			getLogger().error("Cannot send response to caller: " + response, e);
		}
	}

	protected Deferred<Message, Exception> createDeferred() {
		return new CompletableDeferredObject<>();
	}

	private void onDelivery(String consumerTag, @NonNull Delivery delivery) {
		final BasicProperties props = delivery.getProperties();
		final long deliveryTag = delivery.getEnvelope().getDeliveryTag();

		final Message message;

		try {
			message = Message.parse(delivery.getBody());
		} catch (Exception e) {
			getLogger().error("Error while parse delivery body into message (ack will be sent automatically)", e);
			sendAck(deliveryTag);
			sendResponse(e, props);
			return;
		}

		final String correlationId = props == null ? null : props.getCorrelationId();
		final Deferred<Message, Exception> deferred;

		if (correlationId != null || !getQueueConfig().isAutoAck()) {
			deferred = createDeferred();

			if (!queueConfig.isAutoAck()) {
				final boolean ackOnFail = getQueueConfig().isAckOnFail();
				deferred.promise().always((status, response, exception) -> {
					if (status == DeferredStatus.RESOLVED || ackOnFail) {
						this.sendAck(deliveryTag);
					}
				});
			}

			if (correlationId != null) {
				deferred.promise().done((response) -> {
					sendResponse(response, props);
				}).fail((exception) -> {
					sendResponse(exception, props);
				});
			}
		} else {
			deferred = null;
		}

		this.publish(message, deferred);
	}

	private void sendAck(long deliveryTag) {
		try {
			this.getChannel().basicAck(deliveryTag, getQueueConfig().isMultipleAck());
		} catch (IOException e) {
			throw new RuntimeException("Cannot send ack for delivery tag: " + deliveryTag, e);
		}
	}
}
