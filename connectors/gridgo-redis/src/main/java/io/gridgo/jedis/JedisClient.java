package io.gridgo.jedis;

import io.gridgo.connector.redis.RedisClient;
import io.gridgo.framework.AbstractComponentLifecycle;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class JedisClient extends AbstractComponentLifecycle implements RedisClient {

	@Override
	protected void onStart() {

	}

	@Override
	protected String generateName() {
		return null;
	}
}
