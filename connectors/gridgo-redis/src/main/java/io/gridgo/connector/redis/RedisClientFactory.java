package io.gridgo.connector.redis;

public interface RedisClientFactory {

	RedisClient getRedisClient(RedisConfig config);
}
