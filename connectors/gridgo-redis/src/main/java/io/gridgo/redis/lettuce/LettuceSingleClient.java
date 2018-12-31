package io.gridgo.redis.lettuce;

import io.gridgo.redis.RedisConfig;
import io.gridgo.redis.RedisType;
import io.gridgo.redis.lettuce.delegate.LettuceConnectionCommandsDelegate;
import io.gridgo.redis.lettuce.delegate.LettuceTransactionCommandsDelegate;
import io.gridgo.utils.support.HostAndPort;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.RedisURI.Builder;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.async.RedisGeoAsyncCommands;
import io.lettuce.core.api.async.RedisHLLAsyncCommands;
import io.lettuce.core.api.async.RedisHashAsyncCommands;
import io.lettuce.core.api.async.RedisKeyAsyncCommands;
import io.lettuce.core.api.async.RedisListAsyncCommands;
import io.lettuce.core.api.async.RedisScriptingAsyncCommands;
import io.lettuce.core.api.async.RedisSetAsyncCommands;
import io.lettuce.core.api.async.RedisSortedSetAsyncCommands;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.async.RedisTransactionalAsyncCommands;

@SuppressWarnings("unchecked")
public class LettuceSingleClient extends AbstractLettuceClient implements LettuceTransactionCommandsDelegate, LettuceConnectionCommandsDelegate {

    private StatefulRedisConnection<byte[], byte[]> connection;

    private RedisAsyncCommands<byte[], byte[]> commands;

    protected LettuceSingleClient(RedisConfig config) {
        super(RedisType.SINGLE, config);
    }

    protected LettuceSingleClient(RedisType redisType, RedisConfig config) {
        super(redisType, config);
    }

    protected StatefulRedisConnection<byte[], byte[]> createConnection() {
        RedisConfig config = this.getConfig();

        HostAndPort hostAndPort = config.getAddress().getFirst();
        Builder builder = (hostAndPort.getPort() > 0) //
                ? RedisURI.Builder.redis(hostAndPort.getHost(), hostAndPort.getPort()) //
                : RedisURI.Builder.redis(hostAndPort.getHost());

        if (config.getPassword() != null) {
            builder.withPassword(config.getPassword());
        }

        if (config.getDatabase() >= 0) {
            builder.withDatabase(config.getDatabase());
        }

        return RedisClient.create(builder.build()).connect(this.getCodec());
    }

    @Override
    public <T extends RedisAsyncCommands<byte[], byte[]>> T getConnectionCommands() {
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
    public <T extends RedisTransactionalAsyncCommands<byte[], byte[]>> T getTransactionCommands() {
        return (T) this.commands;
    }

    @Override
    protected void onStart() {
        this.connection = this.createConnection();
        this.commands = connection.async();
    }

    @Override
    protected void onStop() {
        this.connection.close();
    }
}
