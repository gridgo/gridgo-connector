package io.gridgo.redis.lettuce.delegate;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.command.RedisSetCommands;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ValueScanCursor;
import io.lettuce.core.api.async.RedisSetAsyncCommands;

public interface LettuceSetCommandsDelegate extends RedisSetCommands, LettuceCommandsDelegate, LettuceScannable {

    <T extends RedisSetAsyncCommands<byte[], byte[]>> T getSetCommands();

    @Override
    default Promise<BElement, Exception> sadd(byte[] key, byte[]... members) {
        return toPromise(getSetCommands().sadd(key, members));
    }

    @Override
    default Promise<BElement, Exception> scard(byte[] key) {
        return toPromise(getSetCommands().scard(key));
    }

    @Override
    default Promise<BElement, Exception> sdiff(byte[]... keys) {
        return toPromise(getSetCommands().sdiff(keys));
    }

    @Override
    default Promise<BElement, Exception> sdiffstore(byte[] destination, byte[]... keys) {
        return toPromise(getSetCommands().sdiffstore(destination, keys));
    }

    @Override
    default Promise<BElement, Exception> sinter(byte[]... keys) {
        return toPromise(getSetCommands().sinter(keys));
    }

    @Override
    default Promise<BElement, Exception> sinterstore(byte[] destination, byte[]... keys) {
        return toPromise(getSetCommands().sinterstore(destination, keys));
    }

    @Override
    default Promise<BElement, Exception> sismember(byte[] key, byte[] member) {
        return toPromise(getSetCommands().sismember(key, member));
    }

    @Override
    default Promise<BElement, Exception> smembers(byte[] key) {
        return toPromise(getSetCommands().smembers(key));
    }

    @Override
    default Promise<BElement, Exception> smove(byte[] source, byte[] destination, byte[] member) {
        return toPromise(getSetCommands().smove(source, destination, member));
    }

    @Override
    default Promise<BElement, Exception> spop(byte[] key) {
        return toPromise(getSetCommands().spop(key));
    }

    @Override
    default Promise<BElement, Exception> spop(byte[] key, long count) {
        return toPromise(getSetCommands().spop(key, count));
    }

    @Override
    default Promise<BElement, Exception> srandmember(byte[] key) {
        return toPromise(getSetCommands().srandmember(key));
    }

    @Override
    default Promise<BElement, Exception> srandmember(byte[] key, long count) {
        return toPromise(getSetCommands().srandmember(key, count));
    }

    @Override
    default Promise<BElement, Exception> srem(byte[] key, byte[]... members) {
        return toPromise(getSetCommands().srem(key, members));
    }

    @Override
    default Promise<BElement, Exception> sscan(byte[] key, String cursor, Long count, String match) {
        ScanCursor scanCursor = cursor == null ? null : ScanCursor.of(cursor);
        ScanArgs scanArgs = buildScanArgs(count, match);

        RedisFuture<ValueScanCursor<byte[]>> future;
        if (scanArgs == null) {
            if (scanCursor == null) {
                future = getSetCommands().sscan(key);
            } else {
                future = getSetCommands().sscan(key, scanCursor);
            }
        } else {
            if (scanCursor == null) {
                future = getSetCommands().sscan(key, scanArgs);
            } else {
                future = getSetCommands().sscan(key, scanCursor, scanArgs);
            }
        }
        return toPromise(future.thenApply(valueScanCursor -> BObject.ofSequence("cursor", valueScanCursor.getCursor(), "values", valueScanCursor.getValues())));
    }

    @Override
    default Promise<BElement, Exception> sunion(byte[]... keys) {
        return toPromise(getSetCommands().sunion(keys));
    }

    @Override
    default Promise<BElement, Exception> sunionstore(byte[] destination, byte[]... keys) {
        return toPromise(getSetCommands().sunionstore(destination, keys));
    }

}
