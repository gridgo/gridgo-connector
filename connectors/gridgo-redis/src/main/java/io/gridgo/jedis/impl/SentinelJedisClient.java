package io.gridgo.jedis.impl;

import java.util.HashSet;
import java.util.Set;

import io.gridgo.connector.redis.RedisConfig;
import lombok.NonNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

public class SentinelJedisClient extends SingleJedisClient {

	private final JedisSentinelPool sentinelPool;

	public SentinelJedisClient(@NonNull RedisConfig config) {
		Set<String> sentinels = new HashSet<>();
		config.getAddress().forEach((address) -> {
			sentinels.add(address.toIpAndPort());
		});
		this.sentinelPool = new JedisSentinelPool(config.getMasterName(), sentinels);
	}

	@Override
	public Jedis getJedis() {
		return this.sentinelPool.getResource();
	}

	@Override
	protected void onStop() {
		try {
			sentinelPool.close();
		} catch (Exception e) {
			throw new RuntimeException("Cannot close jedis sentinel pool", e);
		}
	}
}
