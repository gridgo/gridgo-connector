package io.gridgo.connector.rabbitmq.impl;

import java.util.Map;
import java.util.Optional;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.AsyncDeferredObject;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.rabbitmq.RabbitMQChannelPublisher;
import io.gridgo.connector.rabbitmq.RabbitMQProducer;
import io.gridgo.connector.rabbitmq.RabbitMQQueueConfig;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.MessageParser;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.generators.impl.TimeBasedIdGenerator;
import lombok.Getter;

public abstract class AbstractRabbitMQProducer extends AbstractProducer
		implements RabbitMQProducer, RabbitMQChannelPublisher {

	private static final TimeBasedIdGenerator TIME_BASED_ID_GENERATOR = new TimeBasedIdGenerator();

	private final Connection connection;

	@Getter
	private final RabbitMQQueueConfig queueConfig;

	@Getter
	private Channel channel;

	@Getter
	private String responseQueue;

	private final Map<String, Deferred<Message, Exception>> correlationIdToDeferredMap = new NonBlockingHashMap<>();

	protected AbstractRabbitMQProducer(Connection connection, RabbitMQQueueConfig queueConfig) {
		this.connection = connection;
		this.queueConfig = queueConfig;
	}

	@Override
	protected void onStart() {
		this.channel = this.initChannel(connection);
		if (this.getQueueConfig().isRpc()) {
			this.responseQueue = this.initResponseQueue(this::onResponse);
		}
	}

	@Override
	protected void onStop() {
		this.closeChannel();
	}

	@Override
	public final void send(Message request) {
		Optional<BValue> routingId = request.getRoutingId();
		String routingKey = routingId.or(() -> {
			return Optional.of(BValue.newDefault());
		}).get().getString();
		this.publish(buildBody(request.getPayload()), null, routingKey);
	}

	@Override
	public final Promise<Message, Exception> sendWithAck(Message message) {
		Deferred<Message, Exception> deferred = new AsyncDeferredObject<>();
		this.send(message);
		deferred.resolve(null);
		return deferred.promise();
	}

	private void onResponse(String consumerTag, Delivery delivery) {
		String id = delivery.getProperties().getCorrelationId();
		Deferred<Message, Exception> deferred;
		if ((deferred = this.correlationIdToDeferredMap.get(id)) != null) {
			Message result = MessageParser.DEFAULT.parse(delivery.getBody());
			deferred.resolve(result);
		}
	}

	@Override
	public final Promise<Message, Exception> call(Message request) {
		if (this.queueConfig.isRpc()) {

			Optional<BValue> routingId = request.getRoutingId();
			String routingKey = routingId.isPresent() ? routingId.get().getString() : null;

			String corrId = TIME_BASED_ID_GENERATOR.generateId().get().getString();
			final BasicProperties props = createBasicProperties(corrId);
			byte[] bytes = buildBody(request.getPayload());

			final Deferred<Message, Exception> deferred = createDeferred();
			try {
				this.publish(bytes, props, routingKey);
				this.correlationIdToDeferredMap.put(corrId, deferred);
			} catch (Exception e) {
				deferred.reject(e);
			}
			return deferred.promise();
		}

		throw new UnsupportedOperationException("Cannot make a call on non-rpc rabbitmq producer");
	}

	protected byte[] buildBody(Payload payload) {
		return BArray.newFromSequence(payload.getId().orElse(null), payload.getHeaders(), payload.getBody()).toBytes();
	}

	protected BasicProperties createBasicProperties(String correlationId) {
		return new BasicProperties.Builder() //
				.correlationId(correlationId) //
				.replyTo(this.responseQueue) //
				.build();
	}

	protected Deferred<Message, Exception> createDeferred() {
		return new AsyncDeferredObject<>();
	}

}