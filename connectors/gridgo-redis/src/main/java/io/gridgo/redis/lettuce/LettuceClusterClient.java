package io.gridgo.redis.lettuce;

import static io.lettuce.core.RedisURI.DEFAULT_REDIS_PORT;

import io.gridgo.redis.RedisConfig;
import io.gridgo.redis.RedisType;
import io.gridgo.redis.lettuce.delegate.LettuceClusterConnectionCommandsDelegate;
import io.gridgo.redis.lettuce.delegate.LettuceTransactionCommandsUnsupported;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisGeoAsyncCommands;
import io.lettuce.core.api.async.RedisHLLAsyncCommands;
import io.lettuce.core.api.async.RedisHashAsyncCommands;
import io.lettuce.core.api.async.RedisKeyAsyncCommands;
import io.lettuce.core.api.async.RedisListAsyncCommands;
import io.lettuce.core.api.async.RedisScriptingAsyncCommands;
import io.lettuce.core.api.async.RedisSetAsyncCommands;
import io.lettuce.core.api.async.RedisSortedSetAsyncCommands;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

@SuppressWarnings("unchecked")
public class LettuceClusterClient extends AbstractLettuceClient implements LettuceClusterConnectionCommandsDelegate, LettuceTransactionCommandsUnsupported {

    private RedisAdvancedClusterAsyncCommands<byte[], byte[]> commands;
    private StatefulRedisClusterConnection<byte[], byte[]> connection;

    protected LettuceClusterClient(RedisConfig config) {
        super(RedisType.CLUSTER, config);
    }

    @Override
    public <T extends RedisClusterAsyncCommands<byte[], byte[]>> T getConnectionCommands() {
        return (T) this.commands;
    }

    @Override
    public <T extends RedisGeoAsyncCommands<byte[], byte[]>> T getGeoCommands() {
        return (T) this.commands;
    }

    @Override
    public <T extends RedisHashAsyncCommands<byte[], byte[]>> T getHashCommands() {
        return (T) this.commands;
    }

    @Override
    public <T extends RedisHLLAsyncCommands<byte[], byte[]>> T getHyperLogLogCommands() {
        return (T) this.commands;
    }

    @Override
    public <T extends RedisKeyAsyncCommands<byte[], byte[]>> T getKeysCommands() {
        return (T) this.commands;
    }

    @Override
    public <T extends RedisListAsyncCommands<byte[], byte[]>> T getListCommands() {
        return (T) this.commands;
    }

    @Override
    public <T extends RedisScriptingAsyncCommands<byte[], byte[]>> T getScriptingCommands() {
        return (T) this.commands;
    }

    @Override
    public <T extends RedisSetAsyncCommands<byte[], byte[]>> T getSetCommands() {
        return (T) this.commands;
    }

    @Override
    public <T extends RedisSortedSetAsyncCommands<byte[], byte[]>> T getSortedSetCommands() {
        return (T) this.commands;
    }

    @Override
    public <T extends RedisStringAsyncCommands<byte[], byte[]>> T getStringCommands() {
        return (T) this.commands;
    }

    @Override
    protected void onStart() {
        var config = this.getConfig();

        var uris = config.getAddress().convert(hostAndPort -> {
            RedisURI uri = RedisURI.create(hostAndPort.getHost(), hostAndPort.getPortOrDefault(DEFAULT_REDIS_PORT));

            if (config.getPassword() != null) {
                uri.setPassword(config.getPassword());
            }

            if (config.getDatabase() >= 0) {
                uri.setDatabase(config.getDatabase());
            }

            return uri;
        });

        var client = RedisClusterClient.create(uris);
        this.connection = client.connect(this.getCodec());
        this.commands = this.connection.async();
    }

    @Override
    protected void onStop() {
        this.connection.close();
    }

}
