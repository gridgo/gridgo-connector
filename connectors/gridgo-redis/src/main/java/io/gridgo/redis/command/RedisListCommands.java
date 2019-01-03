package io.gridgo.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisListCommands {

    public Promise<BElement, Exception> blpop(long timeout, byte[]... keys);

    public Promise<BElement, Exception> brpop(long timeout, byte[]... keys);

    public Promise<BElement, Exception> brpoplpush(long timeout, byte[] source, byte[] destination);

    public Promise<BElement, Exception> lindex(byte[] key, long index);

    public Promise<BElement, Exception> linsert(byte[] key, boolean before, byte[] pivot, byte[] value);

    public Promise<BElement, Exception> llen(byte[] key);

    public Promise<BElement, Exception> lpop(byte[] key);

    public Promise<BElement, Exception> lpush(byte[] key, byte[]... values);

    public Promise<BElement, Exception> lpushx(byte[] key, byte[]... values);

    public Promise<BElement, Exception> lrange(byte[] key, long start, long stop);

    public Promise<BElement, Exception> lrem(byte[] key, long count, byte[] value);

    public Promise<BElement, Exception> lset(byte[] key, long index, byte[] value);

    public Promise<BElement, Exception> ltrim(byte[] key, long start, long stop);

    public Promise<BElement, Exception> rpop(byte[] key);

    public Promise<BElement, Exception> rpoplpush(byte[] source, byte[] destination);

    public Promise<BElement, Exception> rpush(byte[] key, byte[]... values);

    public Promise<BElement, Exception> rpushx(byte[] key, byte[]... values);
}
