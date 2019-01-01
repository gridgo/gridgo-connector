package io.gridgo.redis.command;

import java.util.Map;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisHashCommands {

    public Promise<BElement, Exception> hdel(byte[] key, byte[]... fields);

    public Promise<BElement, Exception> hexists(byte[] key, byte[] field);

    public Promise<BElement, Exception> hget(byte[] key, byte[] field);

    public Promise<BElement, Exception> hgetall(byte[] key);

    public Promise<BElement, Exception> hincrby(byte[] key, byte[] field, long amount);

    public Promise<BElement, Exception> hincrbyfloat(byte[] key, byte[] field, double amount);

    public Promise<BElement, Exception> hkeys(byte[] key);

    public Promise<BElement, Exception> hlen(byte[] key);

    public Promise<BElement, Exception> hmget(byte[] key, byte[]... fields);

    public Promise<BElement, Exception> hmset(byte[] key, Map<byte[], byte[]> map);

    public Promise<BElement, Exception> hscan(byte[] key, String cursor, Long count, String match);

    public Promise<BElement, Exception> hset(byte[] key, byte[] field, byte[] value);

    public Promise<BElement, Exception> hsetnx(byte[] key, byte[] field, byte[] value);

    public Promise<BElement, Exception> hstrlen(byte[] key, byte[] field);

    public Promise<BElement, Exception> hvals(byte[] key);
}
