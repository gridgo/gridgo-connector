package io.gridgo.connector.redis.impl;

import java.util.Collection;

import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.redis.RedisConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.redis.RedisClient;
import lombok.NonNull;

@SuppressWarnings("unused")
public class AbstractRedisConsumer extends AbstractConsumer implements RedisConsumer {

    private final RedisClient redisClient;

    private final Collection<String> topics;

    protected AbstractRedisConsumer(@NonNull ConnectorContext context, @NonNull RedisClient redisClient, @NonNull Collection<String> topics) {
        super(context);
        this.redisClient = redisClient;
        this.topics = topics;
    }

    @Override
    protected String generateName() {
        return null;
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }
}
