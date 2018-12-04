package io.gridgo.connector.redis.adapter;

import io.gridgo.connector.redis.adapter.jedis.JedisClientFactory;

public interface RedisClientFactory {

	public static final RedisClientFactory DEFAULT = new JedisClientFactory();

	RedisClient getRedisClient(RedisConfig config);
}
