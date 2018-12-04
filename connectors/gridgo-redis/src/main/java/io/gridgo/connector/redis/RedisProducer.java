package io.gridgo.connector.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.SimpleFailurePromise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.adapter.RedisClientFactory;
import io.gridgo.connector.redis.adapter.RedisConfig;
import io.gridgo.connector.redis.adapter.RedisConstants;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisProducer extends AbstractProducer {

	@Getter
	private final boolean callSupported = true;

	private final RedisConfig config;

	private RedisClient redisClient;
	private Map<String, BiConsumer<Message, Deferred<Message, Exception>>> operations = new HashMap<>();

	public RedisProducer(ConnectorContext context, @NonNull RedisConfig config) {
		super(context);
		this.config = config;
	}

	public void send(Message message) {
	}

	public Promise<Message, Exception> sendWithAck(Message message) {
		return null;
	}

	public Promise<Message, Exception> call(Message request) {
		var deferred = new CompletableDeferredObject<Message, Exception>();
		return _call(request, deferred);
	}

	private Promise<Message, Exception> _call(Message request, CompletableDeferredObject<Message, Exception> deferred) {
		var operation = request.getPayload().getHeaders().getString(RedisConstants.COMMAND);
		var handler = operations.get(operation);
		if (handler == null) {
			return new SimpleFailurePromise<>(
					new IllegalArgumentException("Operation " + operation + " is not supported"));
		}
		try {
			handler.accept(request, deferred);
		} catch (Exception ex) {
			log.error("Error while processing REDIS request", ex);
			deferred.reject(ex);
		}
		return deferred != null ? deferred.promise() : null;
	}

	@Override
	protected String generateName() {
		return null;
	}

	@Override
	protected void onStart() {
		this.redisClient = RedisClientFactory.DEFAULT.getRedisClient(this.config);

		this.operations.put(RedisConstants.COMMAND_APPEND, this::append);
	}

	@Override
	protected void onStop() {
		this.redisClient.stop();
		this.redisClient = null;
	}

	public void append(Message msg, Deferred<Message, Exception> deferred) {
		BObject bOject = msg.getPayload().getBody().asObject();

		getContext().getProducerExecutionStrategy().execute(() -> {
			var key = bOject.getString("key");
			var value = bOject.getString("value");
			deferred.resolve(createMessage(BObject.newDefault(), BElement.fromAny(redisClient.append(key, value))));
		});
	}
}
