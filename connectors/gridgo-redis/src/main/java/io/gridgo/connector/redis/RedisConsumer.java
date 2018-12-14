package io.gridgo.connector.redis;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.redis.impl.DefaultRedisConsumer;
import io.gridgo.connector.support.config.ConnectorContext;

public interface RedisConsumer extends Consumer {

    public static RedisConsumer of(ConnectorContext context) {
        return new DefaultRedisConsumer(context);
    }
}
