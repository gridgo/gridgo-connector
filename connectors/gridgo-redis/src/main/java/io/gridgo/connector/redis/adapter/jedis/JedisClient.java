package io.gridgo.connector.redis.adapter.jedis;

import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.adapter.RedisConfig;
import io.gridgo.framework.AbstractComponentLifecycle;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class JedisClient extends AbstractComponentLifecycle implements RedisClient {

	@Getter(AccessLevel.PROTECTED)
	private final RedisConfig config;

	@Override
	protected String generateName() {
		return null;
	}
}
