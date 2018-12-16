package io.gridgo.connector.redis.adapter.lettuce;

import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.adapter.RedisClientFactory;
import io.gridgo.connector.redis.adapter.RedisConfig;
import io.gridgo.connector.redis.adapter.RedisType;
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
