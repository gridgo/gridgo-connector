package io.gridgo.redis.command;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisKeysCommands {

    public Promise<BElement, Exception> del(byte[]... keys);

    public Promise<BElement, Exception> dump(byte[] key);

    public Promise<BElement, Exception> exists(byte[]... keys);

    public Promise<BElement, Exception> expire(byte[] key, long seconds);

    public Promise<BElement, Exception> expireat(byte[] key, Date timestamp);

    public Promise<BElement, Exception> expireat(byte[] key, long timestamp);

    public Promise<BElement, Exception> keys(byte[] pattern);

    public Promise<BElement, Exception> migrate(String host, int port, int db, long timeout, boolean copy, boolean replace, byte[] keys, String password);

    public Promise<BElement, Exception> move(byte[] key, int db);

    public Promise<BElement, Exception> objectEncoding(byte[] key);

    public Promise<BElement, Exception> objectIdletime(byte[] key);

    public Promise<BElement, Exception> objectRefcount(byte[] key);

    public Promise<BElement, Exception> persist(byte[] key);

    public Promise<BElement, Exception> pexpire(byte[] key, long milliseconds);

    public Promise<BElement, Exception> pexpireat(byte[] key, Date timestamp);

    public Promise<BElement, Exception> pexpireat(byte[] key, long timestamp);

    public Promise<BElement, Exception> pttl(byte[] key);

    public Promise<BElement, Exception> randomkey();

    public Promise<BElement, Exception> rename(byte[] key, byte[] newKey);

    public Promise<BElement, Exception> renamenx(byte[] key, byte[] newKey);

    public Promise<BElement, Exception> restore(byte[] key, byte[] value, long ttl, boolean replace);

    public Promise<BElement, Exception> scan(String cursor, Long count, String match);

    public Promise<BElement, Exception> sort(Consumer<byte[]> channel, byte[] key, String byPattern, List<String> getPatterns, Long count, Long offset,
            String order, boolean alpha);

    public Promise<BElement, Exception> touch(byte[]... keys);

    public Promise<BElement, Exception> ttl(byte[] key);

    public Promise<BElement, Exception> type(byte[] key);

    public Promise<BElement, Exception> unlink(byte[]... keys);
}
