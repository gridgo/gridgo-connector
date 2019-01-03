package io.gridgo.connector.rabbitmq.impl;

import java.util.Map;

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
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.execution.impl.DefaultExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.generators.impl.TimeBasedIdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractRabbitMQProducer extends AbstractProducer implements RabbitMQProducer {

    private static final TimeBasedIdGenerator TIME_BASED_ID_GENERATOR = new TimeBasedIdGenerator();

    private static final ExecutionStrategy DEFAULT_EXECUTION_STRATEGY = new DefaultExecutionStrategy();

    private final Connection connection;

    @Getter
    private final RabbitMQQueueConfig queueConfig;

    @Getter
    private Channel channel;

    @Getter
    private String responseQueue;

    @Getter(AccessLevel.PROTECTED)
    private final String uniqueIdentifier;

    private final Map<String, Deferred<Message, Exception>> correlationIdToDeferredMap = new NonBlockingHashMap<>();

    protected AbstractRabbitMQProducer(@NonNull ConnectorContext context, @NonNull Connection connection, @NonNull RabbitMQQueueConfig queueConfig,
            @NonNull String uniqueIdentifier) {
        super(context);
        this.connection = connection;
        this.queueConfig = queueConfig;
        this.uniqueIdentifier = uniqueIdentifier;
    }

    private void _send(final Message request, final Deferred<Message, Exception> deferred) {
        var routingId = request.getRoutingId();
        var routingKey = routingId.orElse(BValue.ofEmpty()).getString();
        var strategy = getContext().getProducerExecutionStrategy().orElse(DEFAULT_EXECUTION_STRATEGY);

        strategy.execute(() -> {
            this.publish(buildRequestBody(request.getPayload()), null, routingKey);
            if (deferred != null) {
                deferred.resolve(null);
            }
        });
    }

    protected byte[] buildRequestBody(Payload payload) {
        return BArray.ofSequence(payload.getId().orElse(null), payload.getHeaders(), payload.getBody()).toBytes();
    }

    @Override
    public final Promise<Message, Exception> call(Message request) {
        if (this.queueConfig.isRpc()) {

            var routingId = request.getRoutingId();
            var routingKey = routingId.isPresent() ? routingId.get().getString() : null;

            var corrId = TIME_BASED_ID_GENERATOR.generateId().orElseThrow().getString();
            var props = createBasicProperties(corrId);
            var bytes = buildRequestBody(request.getPayload());

            var deferred = createDeferred();

            this.correlationIdToDeferredMap.put(corrId, deferred);
            deferred.promise().always((status, message, ex) -> {
                correlationIdToDeferredMap.remove(corrId);
            });

            var strategy = getContext().getProducerExecutionStrategy() //
                                       .orElse(DEFAULT_EXECUTION_STRATEGY);
            strategy.execute(() -> {
                try {
                    this.publish(bytes, props, routingKey);
                } catch (Exception e) {
                    deferred.reject(e);
                }
            });

            return deferred.promise();
        }

        throw new UnsupportedOperationException("Cannot make a call on non-rpc rabbitmq producer, use rpc=true in connector endpoint");
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

    @Override
    protected String generateName() {
        return null;
    }

    @Override
    public boolean isCallSupported() {
        return true;
    }

    private void onResponse(String consumerTag, Delivery delivery) {
        var id = delivery.getProperties().getCorrelationId();
        var deferred = this.correlationIdToDeferredMap.get(id);
        if (deferred == null)
            return;
        getContext().getCallbackInvokerStrategy().execute(() -> {
            try {
                var result = Message.parse(delivery.getBody());
                deferred.resolve(result);
            } catch (Exception e) {
                deferred.reject(e);
            }
        });
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
    public final void send(@NonNull Message request) {
        this._send(request, null);
    }

    @Override
    public final Promise<Message, Exception> sendWithAck(@NonNull Message message) {
        var deferred = new AsyncDeferredObject<Message, Exception>();
        this._send(message, deferred);
        return deferred.promise();
    }
}