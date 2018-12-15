package io.gridgo.connector.redis.impl;

import java.util.Collection;

import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.support.config.ConnectorContext;

public class DefaultRedisConsumer extends AbstractRedisConsumer {

    public DefaultRedisConsumer(ConnectorContext context, RedisClient redisClient, Collection<String> topics) {
        super(context, redisClient, topics);
    }
}
