package io.gridgo.connector.redis.impl;

import java.util.Collection;

import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.redis.RedisClient;

public class DefaultRedisConsumer extends AbstractRedisConsumer {

    public DefaultRedisConsumer(ConnectorContext context, RedisClient redisClient, Collection<String> topics) {
        super(context, redisClient, topics);
    }
}
