package io.gridgo.connector.redis.adapter.jedis.impl;

import java.util.HashSet;

import io.gridgo.connector.redis.adapter.RedisConfig;
import redis.clients.jedis.JedisSentinelPool;

public class SentinelJedisClient extends PooledJedisClient {

	public SentinelJedisClient(RedisConfig config) {
		super(config);
	}

	@Override
	protected void onStart() {
		RedisConfig config = getConfig();
		HashSet<String> sentinels = new HashSet<String>(config.getAddress().convert(addr -> addr.toIpAndPort()));
		this.setPool(new JedisSentinelPool(config.getMasterName(), sentinels));
	}
}
