package io.gridgo.connector.redis.adapter;

public interface RedisClientFactory {

	RedisClient getRedisClient(RedisConfig config);
}
