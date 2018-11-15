package io.gridgo.connector.rabbitmq.impl;

import java.util.Map;
import java.util.Optional;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.AsyncDeferredObject;
import org.joo.promise4j.impl.CompletableDeferredObject;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.rabbitmq.RabbitMQProducer;
import io.gridgo.connector.rabbitmq.RabbitMQQueueConfig;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.generators.impl.TimeBasedIdGenerator;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractRabbitMQProducer extends AbstractProducer implements RabbitMQProducer {

	private static final TimeBasedIdGenerator TIME_BASED_ID_GENERATOR = new TimeBasedIdGenerator();

	private final Connection connection;

	@Getter
	private final RabbitMQQueueConfig queueConfig;

	@Getter
	private Channel channel;

	@Getter
	private String responseQueue;

	private final Map<String, Deferred<Message, Exception>> correlationIdToDeferredMap = new NonBlockingHashMap<>();

	protected AbstractRabbitMQProducer(ConnectorContext context, Connection connection,
			RabbitMQQueueConfig queueConfig) {
		super(context);
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

	private void _send(final Message request, final Deferred<Message, Exception> deferred) {
		final Optional<BValue> routingId = request.getRoutingId();
		final String routingKey = routingId == null ? null : routingId.orElse(BValue.newDefault()).getString();

		getContext().getProducerExecutionStrategy().execute(() -> {
			this.publish(buildBody(request.getPayload()), null, routingKey);
			if (deferred != null) {
				deferred.resolve(null);
			}
		});
	}

	@Override
	public final void send(@NonNull Message request) {
		this._send(request, null);
	}

	@Override
	public final Promise<Message, Exception> sendWithAck(@NonNull Message message) {
		Deferred<Message, Exception> deferred = new AsyncDeferredObject<>();
		this._send(message, deferred);
		return deferred.promise();
	}

	private void onResponse(String consumerTag, Delivery delivery) {
		String id = delivery.getProperties().getCorrelationId();
		Deferred<Message, Exception> deferred = this.correlationIdToDeferredMap.get(id);
		if (deferred != null) {
			getContext().getCallbackInvokerStrategy().execute(() -> {
				Message result = null;
				try {
					result = Message.parse(delivery.getBody());
				} catch (Exception e) {
					deferred.reject(e);
				}
				if (result != null) {
					deferred.resolve(result);
				}
			});
		}
	}

	@Override
	public final Promise<Message, Exception> call(Message request) {
		if (this.queueConfig.isRpc()) {

			Optional<BValue> routingId = request.getRoutingId();
			String routingKey = routingId.isPresent() ? routingId.get().getString() : null;

			final String corrId = TIME_BASED_ID_GENERATOR.generateId().get().getString();
			final BasicProperties props = createBasicProperties(corrId);
			byte[] bytes = buildBody(request.getPayload());

			final Deferred<Message, Exception> deferred = createDeferred();

			this.correlationIdToDeferredMap.put(corrId, deferred);
			deferred.promise().always((status, message, ex) -> {
				correlationIdToDeferredMap.remove(corrId);
			});

			getContext().getProducerExecutionStrategy().execute(() -> {
				try {
					this.publish(bytes, props, routingKey);
				} catch (Exception e) {
					deferred.reject(e);
				}
			});

			return deferred.promise();
		}

		throw new UnsupportedOperationException(
				"Cannot make a call on non-rpc rabbitmq producer, use rpc=true in connector endpoint");
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
		return new CompletableDeferredObject<>();
	}

}