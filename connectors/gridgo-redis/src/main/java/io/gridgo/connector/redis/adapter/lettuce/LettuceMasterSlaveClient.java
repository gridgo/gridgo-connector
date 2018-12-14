package io.gridgo.connector.redis.adapter.lettuce;

import static io.lettuce.core.RedisURI.DEFAULT_REDIS_PORT;

import java.util.Collection;

import io.gridgo.connector.redis.adapter.RedisConfig;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.masterslave.MasterSlave;

public class LettuceMasterSlaveClient extends LettuceSingleClient {

    protected LettuceMasterSlaveClient(RedisConfig config) {
        super(config);
    }

    @Override
    protected StatefulRedisConnection<byte[], byte[]> createConnection() {
        RedisConfig config = this.getConfig();

        Collection<RedisURI> uris = config.getAddress().convert(hostAndPort -> {
            RedisURI uri = RedisURI.create(hostAndPort.getHost(), hostAndPort.getPortOrDefault(DEFAULT_REDIS_PORT));

            if (config.getPassword() != null) {
                uri.setPassword(config.getPassword());
            }

            if (config.getDatabase() >= 0) {
                uri.setDatabase(config.getDatabase());
            }

            return uri;
        });

        RedisClient client = RedisClient.create();
        return MasterSlave.connect(client, getCodec(), uris);
    }
}
