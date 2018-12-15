package io.gridgo.connector.redis;

import java.util.Collection;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.impl.DefaultRedisConsumer;
import io.gridgo.connector.support.config.ConnectorContext;

public interface RedisConsumer extends Consumer {

    public static RedisConsumer of(ConnectorContext context, RedisClient redisClient, Collection<String> topics) {
        return new DefaultRedisConsumer(context, redisClient, topics);
    }
}
