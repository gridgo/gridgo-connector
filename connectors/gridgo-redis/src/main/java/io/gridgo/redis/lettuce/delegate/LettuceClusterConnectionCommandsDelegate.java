package io.gridgo.redis.lettuce.delegate;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.redis.command.RedisConnectionCommands;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

public interface LettuceClusterConnectionCommandsDelegate extends LettuceCommandsDelegate, RedisConnectionCommands {

    @Override
    default String auth(String password) {
        return getConnectionCommands().auth(password);
    }

    @Override
    default Promise<BElement, Exception> echo(byte[] msg) {
        return toPromise(getConnectionCommands().echo(msg));
    }

    <T extends RedisClusterAsyncCommands<byte[], byte[]>> T getConnectionCommands();

    @Override
    default Promise<BElement, Exception> ping() {
        return toPromise(getConnectionCommands().ping());
    }

    @Override
    default Promise<BElement, Exception> quit() {
        return toPromise(getConnectionCommands().quit());
    }

    @Override
    default String select(int db) {
        throw new UnsupportedOperationException("Method is unsupported in redis cluster");
    }

    @Override
    default Promise<BElement, Exception> swapdb(int db1, int db2) {
        throw new UnsupportedOperationException("Method is unsupported in redis cluster");
    }
}
