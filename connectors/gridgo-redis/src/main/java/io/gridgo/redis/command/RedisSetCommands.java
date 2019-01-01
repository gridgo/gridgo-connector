package io.gridgo.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisSetCommands {

    public Promise<BElement, Exception> sadd(byte[] key, byte[]... members);

    public Promise<BElement, Exception> scard(byte[] key);

    public Promise<BElement, Exception> sdiff(byte[]... keys);

    public Promise<BElement, Exception> sdiffstore(byte[] destination, byte[]... keys);

    public Promise<BElement, Exception> sinter(byte[]... keys);

    public Promise<BElement, Exception> sinterstore(byte[] destination, byte[]... keys);

    public Promise<BElement, Exception> sismember(byte[] key, byte[] member);

    public Promise<BElement, Exception> smembers(byte[] key);

    public Promise<BElement, Exception> smove(byte[] source, byte[] destination, byte[] member);

    public Promise<BElement, Exception> spop(byte[] key);

    public Promise<BElement, Exception> spop(byte[] key, long count);

    public Promise<BElement, Exception> srandmember(byte[] key);

    public Promise<BElement, Exception> srandmember(byte[] key, long count);

    public Promise<BElement, Exception> srem(byte[] key, byte[]... members);

    public Promise<BElement, Exception> sscan(byte[] key, String cursor, Long count, String match);

    public Promise<BElement, Exception> sunion(byte[]... keys);

    public Promise<BElement, Exception> sunionstore(byte[] destination, byte[]... keys);
}
