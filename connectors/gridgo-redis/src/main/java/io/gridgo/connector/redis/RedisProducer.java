package io.gridgo.connector.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;

import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Slf4j
public class RedisProducer extends AbstractProducer {
	
	private final Jedis jedis;
	private Map<String, BiConsumer<Message, Deferred<Message, Exception>>> operations = new HashMap<>();

	protected RedisProducer(ConnectorContext context, String host, String port) {
		super(context);
		jedis = new Jedis(host, Integer.parseInt(port));
	}
	
	public void bind(String name, BiConsumer<Message, Deferred<Message, Exception>> handler) {
		operations.put(name, handler);
	}

	public void send(Message message) {
		
	}

	public Promise<Message, Exception> sendWithAck(Message message) {
		return null;
	}

	public Promise<Message, Exception> call(Message request) {
		return null;
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
		bind(RedisConstants.COMMAND_APPEND, this::append);
	}

	@Override
	protected void onStop() {
		
	}
	
	public void append(Message msg, Deferred<Message, Exception> deferred) {
		
	}

}
