package io.gridgo.redis.lettuce;

import io.gridgo.redis.RedisClient;
import io.gridgo.redis.RedisClientFactory;
import io.gridgo.redis.RedisConfig;
import io.gridgo.redis.RedisType;
import lombok.NonNull;

public class LettuceClientFactory implements RedisClientFactory {

    @Override
    public RedisClient newClient(@NonNull RedisType type, @NonNull RedisConfig config) {
        switch (type) {
        case SINGLE:
            return new LettuceSingleClient(config);
        case MASTER_SLAVE:
            return new LettuceMasterSlaveClient(config);
        case CLUSTER:
            return new LettuceClusterClient(config);
        case SENTINEL:
            return new LettuceSentinelClient(config);
        default:
            break;
        }
        return null;
    }

}
