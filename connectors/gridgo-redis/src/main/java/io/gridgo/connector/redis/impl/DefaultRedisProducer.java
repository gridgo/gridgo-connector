package io.gridgo.connector.redis.impl;

import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.support.config.ConnectorContext;

public class DefaultRedisProducer extends AbstractRedisProducer {

    public DefaultRedisProducer(ConnectorContext context, RedisClient redisClient) {
        super(context, redisClient);
    }

}
