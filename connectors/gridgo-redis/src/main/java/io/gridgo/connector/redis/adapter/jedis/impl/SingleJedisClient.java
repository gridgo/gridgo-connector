package io.gridgo.connector.redis.adapter.jedis.impl;

import io.gridgo.connector.redis.adapter.RedisConfig;
import io.gridgo.utils.support.HostAndPort;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class SingleJedisClient extends PooledJedisClient {

	public SingleJedisClient(RedisConfig config) {
		super(config);
	}

	@Override
	protected void onStart() {
		RedisConfig config = this.getConfig();
		HostAndPort hostAndPort = config.getAddress().getFirst();
		this.setPool(new JedisPool( //
				new JedisPoolConfig(), //
				hostAndPort.getHost(), //
				hostAndPort.getPort(), //
				config.getTimeout(), //
				config.getPassword()));
	}
}
