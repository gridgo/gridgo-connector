package io.gridgo.connector.redis.impl;

import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.redis.RedisClient;

public class DefaultRedisProducer extends AbstractRedisProducer {

    public DefaultRedisProducer(ConnectorContext context, RedisClient redisClient) {
        super(context, redisClient);
    }

}
