package io.gridgo.connector.redis;

import io.gridgo.connector.Producer;
import io.gridgo.connector.redis.impl.DefaultRedisProducer;
import io.gridgo.connector.support.config.ConnectorContext;

public interface RedisProducer extends Producer {

    public static RedisProducer of(ConnectorContext context) {
        return new DefaultRedisProducer(context);
    }
}
