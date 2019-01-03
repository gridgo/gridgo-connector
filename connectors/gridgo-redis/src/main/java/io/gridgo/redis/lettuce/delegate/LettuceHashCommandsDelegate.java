package io.gridgo.redis.lettuce.delegate;

import java.util.Map;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.redis.command.RedisHashCommands;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.async.RedisHashAsyncCommands;

public interface LettuceHashCommandsDelegate extends LettuceCommandsDelegate, RedisHashCommands, LettuceScannable {

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
    default Promise<BElement, Exception> hget(byte[] key, byte[] field) {
        return toPromise(getHashCommands().hget(key, field));
    }

    @Override
    default Promise<BElement, Exception> hgetall(byte[] key) {
        return toPromise(getHashCommands().hgetall(key));
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
        return toPromise(getHashCommands().hmget(key, fields) //
                                          .thenApply(list -> this.convertList(list, this::keyValueToBArray)));
    }

    @Override
    default Promise<BElement, Exception> hmset(byte[] key, Map<byte[], byte[]> map) {
        return toPromise(getHashCommands().hmset(key, map));
    }

    @Override
    default Promise<BElement, Exception> hscan(byte[] key, String cursor, Long count, String match) {
        ScanCursor scanCursor = cursor == null ? null : ScanCursor.of(cursor);
        ScanArgs scanArgs = buildScanArgs(count, match);

        RedisFuture<MapScanCursor<byte[], byte[]>> future;
        if (scanCursor == null) {
            if (scanArgs == null) {
                future = getHashCommands().hscan(key);
            } else {
                future = getHashCommands().hscan(key, scanArgs);
            }
        } else {
            if (scanArgs == null) {
                future = getHashCommands().hscan(key, scanCursor);
            } else {
                future = getHashCommands().hscan(key, scanCursor, scanArgs);
            }
        }

        return toPromise(future.thenApply(mapScanCursor -> BArray.ofSequence(mapScanCursor.getCursor(), mapScanCursor.getMap())));
    }

    @Override
    default Promise<BElement, Exception> hset(byte[] key, byte[] field, byte[] value) {
        return toPromise(getHashCommands().hset(key, field, value));
    }

    @Override
    default Promise<BElement, Exception> hsetnx(byte[] key, byte[] field, byte[] value) {
        return toPromise(getHashCommands().hsetnx(key, field, value));
    }

    @Override
    default Promise<BElement, Exception> hstrlen(byte[] key, byte[] field) {
        return toPromise(getHashCommands().hstrlen(key, field));
    }

    @Override
    default Promise<BElement, Exception> hvals(byte[] key) {
        return toPromise(getHashCommands().hvals(key));
    }
}
