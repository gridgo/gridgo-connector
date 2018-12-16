package io.gridgo.connector.redis.adapter.lettuce;

import io.gridgo.connector.redis.adapter.RedisConfig;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

public class LettuceSentinnelClient extends LettuceSingleClient {

    protected LettuceSentinnelClient(RedisConfig config) {
        super(config);
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
