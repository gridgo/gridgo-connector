package io.gridgo.jedis;

import io.gridgo.connector.redis.RedisClient;
import io.gridgo.connector.redis.RedisClientFactory;
import io.gridgo.connector.redis.RedisConfig;
import io.gridgo.connector.redis.RedisServerType;
import io.gridgo.jedis.impl.ClusterJedisClient;
import io.gridgo.jedis.impl.SentinelJedisClient;
import io.gridgo.jedis.impl.SingleJedisClient;

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
