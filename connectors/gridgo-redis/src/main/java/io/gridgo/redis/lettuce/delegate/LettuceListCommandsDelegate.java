package io.gridgo.redis.lettuce.delegate;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.redis.command.RedisListCommands;
import io.lettuce.core.api.async.RedisListAsyncCommands;

public interface LettuceListCommandsDelegate extends LettuceCommandsDelegate, RedisListCommands {

    @Override
    default Promise<BElement, Exception> blpop(long timeout, byte[]... keys) {
        return toPromise(getListCommands().blpop(timeout, keys) //
                                          .thenApply(this::keyValueToBArray));
    }

    @Override
    default Promise<BElement, Exception> brpop(long timeout, byte[]... keys) {
        return toPromise(getListCommands().brpop(timeout, keys) //
                                          .thenApply(this::keyValueToBArray));
    }

    @Override
    default Promise<BElement, Exception> brpoplpush(long timeout, byte[] source, byte[] destination) {
        return toPromise(getListCommands().brpoplpush(timeout, source, destination));
    }

    <T extends RedisListAsyncCommands<byte[], byte[]>> T getListCommands();

    @Override
    default Promise<BElement, Exception> lindex(byte[] key, long index) {
        return toPromise(getListCommands().lindex(key, index));
    }

    @Override
    default Promise<BElement, Exception> linsert(byte[] key, boolean before, byte[] pivot, byte[] value) {
        return toPromise(getListCommands().linsert(key, before, pivot, value));
    }

    @Override
    default Promise<BElement, Exception> llen(byte[] key) {
        return toPromise(getListCommands().llen(key));
    }

    @Override
    default Promise<BElement, Exception> lpop(byte[] key) {
        return toPromise(getListCommands().lpop(key));
    }

    @Override
    default Promise<BElement, Exception> lpush(byte[] key, byte[]... values) {
        return toPromise(getListCommands().lpush(key, values));
    }

    @Override
    default Promise<BElement, Exception> lpushx(byte[] key, byte[]... values) {
        return toPromise(getListCommands().lpushx(key, values));
    }

    @Override
    default Promise<BElement, Exception> lrange(byte[] key, long start, long stop) {
        return toPromise(getListCommands().lrange(key, start, stop));
    }

    @Override
    default Promise<BElement, Exception> lrem(byte[] key, long count, byte[] value) {
        return toPromise(getListCommands().lrem(key, count, value));
    }

    @Override
    default Promise<BElement, Exception> lset(byte[] key, long index, byte[] value) {
        return toPromise(getListCommands().lset(key, index, value));
    }

    @Override
    default Promise<BElement, Exception> ltrim(byte[] key, long start, long stop) {
        return toPromise(getListCommands().ltrim(key, start, stop));
    }

    @Override
    default Promise<BElement, Exception> rpop(byte[] key) {
        return toPromise(getListCommands().rpop(key));
    }

    @Override
    default Promise<BElement, Exception> rpoplpush(byte[] source, byte[] destination) {
        return toPromise(getListCommands().rpoplpush(source, destination));
    }

    @Override
    default Promise<BElement, Exception> rpush(byte[] key, byte[]... values) {
        return toPromise(getListCommands().rpush(key, values));
    }

    @Override
    default Promise<BElement, Exception> rpushx(byte[] key, byte[]... values) {
        return toPromise(getListCommands().rpushx(key, values));
    }
}
