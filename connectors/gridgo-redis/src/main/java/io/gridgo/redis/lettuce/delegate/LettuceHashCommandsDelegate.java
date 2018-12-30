package io.gridgo.redis.lettuce.delegate;

import java.util.Map;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.redis.command.RedisHashCommands;
import io.lettuce.core.api.async.RedisHashAsyncCommands;

public interface LettuceHashCommandsDelegate extends LettuceCommandsDelegate, RedisHashCommands {

    <T extends RedisHashAsyncCommands<byte[], byte[]>> T getHashCommands();

    @Override
    default Promise<BElement, Exception> hdel(byte[] key, byte[]... fields) {
        return toPromise(getHashCommands().hdel(key, fields));
    }

    @Override
    default Promise<BElement, Exception> hexists(byte[] key, byte[] field) {
        return toPromise(getHashCommands().hexists(key, field));
    }

    @Override
    default Promise<BElement, Exception> hincrby(byte[] key, byte[] field, long amount) {
        return toPromise(getHashCommands().hincrby(key, field, amount));
    }

    @Override
    default Promise<BElement, Exception> hincrbyfloat(byte[] key, byte[] field, double amount) {
        return toPromise(getHashCommands().hincrbyfloat(key, field, amount));
    }

    @Override
    default Promise<BElement, Exception> hkeys(byte[] key) {
        return toPromise(getHashCommands().hkeys(key));
    }

    @Override
    default Promise<BElement, Exception> hlen(byte[] key) {
        return toPromise(getHashCommands().hlen(key));
    }

    @Override
    default Promise<BElement, Exception> hmget(byte[] key, byte[]... fields) {
        return toPromise(getHashCommands().hmget(key, fields));
    }

    @Override
    default Promise<BElement, Exception> hmset(byte[] key, Map<byte[], byte[]> map) {
        return toPromise(getHashCommands().hmset(key, map));
    }

    @Override
    default Promise<BElement, Exception> hscan(byte[] key) {
        return toPromise(getHashCommands().hscan(key));
    }

    @Override
    default Promise<BElement, Exception> hset(byte[] key, byte[] field, byte[] value) {
        return toPromise(getHashCommands().hset(key, field, value));
    }

    @Override
    default Promise<BElement, Exception> hstrlen(byte[] key, byte[] field) {
        return toPromise(getHashCommands().hstrlen(key, field));
    }

    @Override
    default Promise<BElement, Exception> hvals(byte[] key) {
        return toPromise(getHashCommands().hvals(key));
    }

    @Override
    default Promise<BElement, Exception> hget(byte[] key, byte[] field) {
        return toPromise(getHashCommands().hget(key, field));
    }

    @Override
    default Promise<BElement, Exception> hgetall(byte[] key) {
        return toPromise(getHashCommands().hgetall(key));
    }

    @Override
    default Promise<BElement, Exception> hsetnx(byte[] key, byte[] field, byte[] value) {
        return toPromise(getHashCommands().hsetnx(key, field, value));
    }
}
