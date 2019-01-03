package io.gridgo.connector.redis;

import io.gridgo.connector.Producer;
import io.gridgo.connector.redis.impl.DefaultRedisProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.redis.RedisClient;

public interface RedisProducer extends Producer {

    public static RedisProducer of(ConnectorContext context, RedisClient redisClient) {
        return new DefaultRedisProducer(context, redisClient);
    }
}
