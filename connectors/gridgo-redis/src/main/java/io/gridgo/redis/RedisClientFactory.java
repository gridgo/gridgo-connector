package io.gridgo.redis;

import io.gridgo.redis.lettuce.LettuceClientFactory;

public interface RedisClientFactory {

    public static RedisClientFactory newDefault() {
        return new LettuceClientFactory();
    }

    public RedisClient newClient(RedisType type, RedisConfig config);
}
