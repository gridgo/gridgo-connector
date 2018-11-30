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
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisProducer extends AbstractProducer {

	private RedisClient redisClient;
	private Map<String, BiConsumer<Message, Deferred<Message, Exception>>> operations = new HashMap<>();

	protected RedisProducer(ConnectorContext context, RedisClient redisClient) {
		super(context);
		this.redisClient = redisClient;
	}

	protected void bind(String name, BiConsumer<Message, Deferred<Message, Exception>> handler) {
		operations.put(name, handler);
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
		// đầu tiên, anh thấy em định nghĩa command trong header. Nếu là command, anh thấy nên đặt một lớp riêng kiểu RedisCommand thay vì đặt chung vào constats
		// chỗ này nó có tính nghiệp vụ cao hơn so với cái const 
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

	public boolean isCallSupported() {
		return false;
	}

	@Override
	protected String generateName() {
		return null;
	}

	@Override
	protected void onStart() {
		// em định bind từng cmd ở đây luôn á??? :| hie tnai thi vang a
		// đệch :| :| Cai nay lam sao cho hop ly at
		// thường thì tất cả cmd đều có sẵn bên cái RedisClient impl rồi đúng ko?
		// giờ em chỉ cần có một cách nào đó lôi được cái hàm tương ứng ra mà xài?
		// neu ma k bind kieu nay thi van fai switch case ma :-? 
		// chỗ 
		bind(RedisConstants.COMMAND_APPEND, this::append);
	}

	@Override
	protected void onStop() {

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
