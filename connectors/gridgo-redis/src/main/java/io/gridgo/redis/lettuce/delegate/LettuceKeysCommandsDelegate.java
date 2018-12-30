package io.gridgo.redis.lettuce.delegate;

import java.util.Date;
import java.util.List;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.redis.RedisKeysCommands;
import io.lettuce.core.MigrateArgs;
import io.lettuce.core.RestoreArgs;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.SortArgs;
import io.lettuce.core.api.async.RedisKeyAsyncCommands;

public interface LettuceKeysCommandsDelegate extends LettuceCommandsDelegate, RedisKeysCommands {

    <T extends RedisKeyAsyncCommands<byte[], byte[]>> T getKeysCommands();

    @Override
    default Promise<BElement, Exception> dump(byte[] key) {
        return toPromise(getKeysCommands().dump(key));
    }

    @Override
    default Promise<BElement, Exception> exists(byte[]... keys) {
        return toPromise(getKeysCommands().exists(keys));
    }

    @Override
    default Promise<BElement, Exception> expireat(byte[] key, Date timestamp) {
        return toPromise(getKeysCommands().expireat(key, timestamp));
    }

    @Override
    default Promise<BElement, Exception> expireat(byte[] key, long timestamp) {
        return toPromise(getKeysCommands().expireat(key, timestamp));
    }

    @Override
    default Promise<BElement, Exception> expire(byte[] key, long seconds) {
        return toPromise(getKeysCommands().expire(key, seconds));
    }

    @Override
    default Promise<BElement, Exception> keys(byte[] pattern) {
        return toPromise(getKeysCommands().keys(pattern));
    }

    @Override
    default Promise<BElement, Exception> migrate(String host, int port, int db, long timeout, boolean copy, boolean replace, byte[] keys, String password) {
        MigrateArgs<byte[]> migrateArgs = MigrateArgs.Builder.key(keys).auth(password == null ? null : password.toCharArray());
        if (copy) {
            migrateArgs.copy();
        }
        if (replace) {
            migrateArgs.replace();
        }
        return toPromise(getKeysCommands().migrate(host, port, db, timeout, migrateArgs));
    }

    @Override
    default Promise<BElement, Exception> move(byte[] key, int db) {
        return toPromise(getKeysCommands().move(key, db));
    }

    @Override
    default Promise<BElement, Exception> persist(byte[] key) {
        return toPromise(getKeysCommands().persist(key));
    }

    @Override
    default Promise<BElement, Exception> pexpire(byte[] key, long milliseconds) {
        return toPromise(getKeysCommands().pexpire(key, milliseconds));
    }

    @Override
    default Promise<BElement, Exception> pexpireat(byte[] key, Date timestamp) {
        return toPromise(getKeysCommands().pexpireat(key, timestamp));
    }

    @Override
    default Promise<BElement, Exception> pexpireat(byte[] key, long timestamp) {
        return toPromise(getKeysCommands().pexpireat(key, timestamp));
    }

    @Override
    default Promise<BElement, Exception> pttl(byte[] key) {
        return toPromise(getKeysCommands().pttl(key));
    }

    @Override
    default Promise<BElement, Exception> rename(byte[] key, byte[] newKey) {
        return toPromise(getKeysCommands().rename(key, newKey));
    }

    @Override
    default Promise<BElement, Exception> restore(byte[] key, byte[] value, long ttl, boolean replace) {
        RestoreArgs args = RestoreArgs.Builder.ttl(ttl);
        if (replace) {
            args.replace();
        }
        return toPromise(getKeysCommands().restore(key, value, args));
    }

    @Override
    default Promise<BElement, Exception> sort(java.util.function.Consumer<byte[]> channel, byte[] key, String byPattern, List<String> getPatterns, Long count,
            Long offset, String order, boolean alpha) {
        SortArgs sortArgs = buildSortArgs(byPattern, getPatterns, count, offset, order, alpha);

        return toPromise(getKeysCommands().sort(bytes -> channel.accept(bytes), key, sortArgs));
    }

    @Override
    default Promise<BElement, Exception> touch(byte[]... keys) {
        return toPromise(getKeysCommands().touch(keys));
    }

    @Override
    default Promise<BElement, Exception> ttl(byte[] key) {
        return toPromise(getKeysCommands().ttl(key));
    }

    @Override
    default Promise<BElement, Exception> del(byte[]... keys) {
        return toPromise(getKeysCommands().del(keys));
    }

    @Override
    default Promise<BElement, Exception> type(byte[] key) {
        return toPromise(getKeysCommands().type(key));
    }

    @Override
    default Promise<BElement, Exception> scan(java.util.function.Consumer<byte[]> channel, String cursor, Long count, String match) {
        ScanCursor scanCursor = cursor == null ? null : ScanCursor.of(cursor);
        if (count != null || match != null) {
            ScanArgs args = new ScanArgs();
            if (count != null) {
                args.limit(count);
            }
            if (match != null) {
                args.match(match);
            }
            if (cursor == null) {
                return toPromise(getKeysCommands().scan(bytes -> channel.accept(bytes), args));
            }
            return toPromise(getKeysCommands().scan(bytes -> channel.accept(bytes), scanCursor, args));
        }
        if (cursor == null) {
            return toPromise(getKeysCommands().scan(bytes -> channel.accept(bytes)));
        }
        return toPromise(getKeysCommands().scan(bytes -> channel.accept(bytes), scanCursor));
    }

    @Override
    default Promise<BElement, Exception> unlink(byte[]... keys) {
        return toPromise(getKeysCommands().unlink(keys));
    }

    @Override
    default Promise<BElement, Exception> randomkey() {
        return toPromise(getKeysCommands().randomkey());
    }

    @Override
    default Promise<BElement, Exception> renamenx(byte[] key, byte[] newKey) {
        return toPromise(getKeysCommands().renamenx(key, newKey));
    }

    @Override
    default Promise<BElement, Exception> objectRefcount(byte[] key) {
        return toPromise(getKeysCommands().objectRefcount(key));
    }

    @Override
    default Promise<BElement, Exception> objectEncoding(byte[] key) {
        return toPromise(getKeysCommands().objectEncoding(key));
    }

    @Override
    default Promise<BElement, Exception> objectIdletime(byte[] key) {
        return toPromise(getKeysCommands().objectIdletime(key));
    }
}
