package io.gridgo.connector.redis.adapter.jedis;

import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.adapter.RedisClientFactory;
import io.gridgo.connector.redis.adapter.RedisConfig;
import io.gridgo.connector.redis.adapter.RedisServerType;
import io.gridgo.connector.redis.adapter.jedis.impl.ClusterJedisClient;
import io.gridgo.connector.redis.adapter.jedis.impl.SentinelJedisClient;
import io.gridgo.connector.redis.adapter.jedis.impl.SingleJedisClient;

public class JedisClientFactory implements RedisClientFactory {

	@Override
	public RedisClient getRedisClient(RedisConfig config) {
		RedisServerType serverType = config.getServerType();
		if (serverType != null) {
			switch (serverType) {
			case SINGLE:
				return new SingleJedisClient(config);
			case CLUSTER:
				return new ClusterJedisClient(config);
			case SENTINEL:
				return new SentinelJedisClient(config);
			}
		}
		throw new RuntimeException("Redis server type not found");
	}
}
