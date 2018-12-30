package io.gridgo.redis.lettuce;

import io.gridgo.redis.RedisConfig;
import io.gridgo.redis.RedisType;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

public class LettuceSentinelClient extends LettuceSingleClient {

    protected LettuceSentinelClient(RedisConfig config) {
        super(RedisType.SENTINEL, config);
    }

    @Override
    protected StatefulRedisConnection<byte[], byte[]> createConnection() {
        RedisURI.Builder builder = RedisURI.builder().withSentinelMasterId(getConfig().getSentinelMasterId());
        getConfig().getAddress().forEach(addr -> {
            builder.withSentinel(addr.getHost(), addr.getPortOrDefault(26379));
        });

        builder.withPassword(getConfig().getPassword());

        if (getConfig().getDatabase() >= 0) {
            builder.withDatabase(getConfig().getDatabase());
        }

        RedisURI redisUri = builder.build();
        RedisClient client = RedisClient.create(redisUri);

        return client.connect(getCodec());
    }
}
