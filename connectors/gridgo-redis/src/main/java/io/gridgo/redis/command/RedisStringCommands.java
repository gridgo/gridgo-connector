package io.gridgo.redis.command;

import java.util.Map;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisStringCommands {

    public Promise<BElement, Exception> append(byte[] key, byte[] value);

    public Promise<BElement, Exception> bitcount(byte[] key, long start, long end);

    public Promise<BElement, Exception> bitfield(byte[] key, String overflow, Object... subCommandAndArgs);

    public Promise<BElement, Exception> bitopAnd(byte[] destination, byte[]... keys);

    public Promise<BElement, Exception> bitopNot(byte[] destination, byte[] source);

    public Promise<BElement, Exception> bitopOr(byte[] destination, byte[]... keys);

    public Promise<BElement, Exception> bitopXor(byte[] destination, byte[]... keys);

    public Promise<BElement, Exception> bitpos(byte[] key, boolean state);

    public Promise<BElement, Exception> bitpos(byte[] key, boolean state, long start);

    public Promise<BElement, Exception> bitpos(byte[] key, boolean state, long start, long end);

    public Promise<BElement, Exception> decr(byte[] key);

    public Promise<BElement, Exception> decrby(byte[] key, long amount);

    public Promise<BElement, Exception> get(byte[] key);

    public Promise<BElement, Exception> getbit(byte[] key, long offset);

    public Promise<BElement, Exception> getrange(byte[] key, long start, long end);

    public Promise<BElement, Exception> getset(byte[] key, byte[] value);

    public Promise<BElement, Exception> incr(byte[] key);

    public Promise<BElement, Exception> incrby(byte[] key, long amount);

    public Promise<BElement, Exception> incrbyfloat(byte[] key, double amount);

    public Promise<BElement, Exception> mget(byte[]... keys);

    public Promise<BElement, Exception> mset(Map<byte[], byte[]> map);

    public Promise<BElement, Exception> msetnx(Map<byte[], byte[]> map);

    public Promise<BElement, Exception> psetex(byte[] key, long milliseconds, byte[] value);

    public Promise<BElement, Exception> set(byte[] key, byte[] value);

    public Promise<BElement, Exception> setbit(byte[] key, long offset, int value);

    public Promise<BElement, Exception> setex(byte[] key, long seconds, byte[] value);

    public Promise<BElement, Exception> setnx(byte[] key, byte[] value);

    public Promise<BElement, Exception> setrange(byte[] key, long offset, byte[] value);

    public Promise<BElement, Exception> strlen(byte[] key);
}
