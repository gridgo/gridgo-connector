package io.gridgo.redis.command;

import java.util.List;
import java.util.function.Consumer;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisSortedSetCommands {

    public Promise<BElement, Exception> bzpopmax(long timeout, byte[]... keys);

    public Promise<BElement, Exception> bzpopmin(long timeout, byte[]... keys);

    public Promise<BElement, Exception> zadd(byte[] key, boolean xx, boolean nx, boolean ch, Object... scoresAndValues);

    public Promise<BElement, Exception> zadd(byte[] key, double score, byte[] member);

    public Promise<BElement, Exception> zadd(byte[] key, Object... scoresAndValues);

    public Promise<BElement, Exception> zcard(byte[] key);

    public Promise<BElement, Exception> zcount(byte[] key, boolean includeLower, long lower, long upper, boolean includeUpper);

    public Promise<BElement, Exception> zincrby(byte[] key, double amount, byte[] member);

    public Promise<BElement, Exception> zinterstore(byte[] destination, String aggregate, List<Double> weights, byte[]... keys);

    public Promise<BElement, Exception> zlexcount(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper);

    public Promise<BElement, Exception> zpopmax(byte[] key);

    public Promise<BElement, Exception> zpopmax(byte[] key, long count);

    public Promise<BElement, Exception> zpopmin(byte[] key);

    public Promise<BElement, Exception> zpopmin(byte[] key, long count);

    public Promise<BElement, Exception> zrange(byte[] key, long start, long stop);

    public Promise<BElement, Exception> zrange(Consumer<byte[]> valueConsumer, byte[] key, long start, long stop);

    public Promise<BElement, Exception> zrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper, Long offset,
            Long count);

    public Promise<BElement, Exception> zrangebyscore(Consumer<byte[]> channel, byte[] key, boolean includeLower, long lower, long upper, boolean includeUpper,
            Long offset, Long count);

    public Promise<BElement, Exception> zrangeWithScores(byte[] key, long start, long stop);

    public Promise<BElement, Exception> zrank(byte[] key, byte[] member);

    public Promise<BElement, Exception> zrem(byte[] key, byte[]... members);

    public Promise<BElement, Exception> zremrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper);

    public Promise<BElement, Exception> zremrangebyrank(byte[] key, long start, long stop);

    public Promise<BElement, Exception> zremrangebyscore(byte[] key, boolean includeLower, long lower, long upper, boolean includeUpper);

    public Promise<BElement, Exception> zrevrange(Consumer<byte[]> channel, byte[] key, long start, long stop);

    public Promise<BElement, Exception> zrevrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper, Long offset,
            Long count);

    public Promise<BElement, Exception> zrevrangebyscore(Consumer<byte[]> channel, byte[] key, boolean includeLower, long lower, long upper,
            boolean includeUpper, Long offset, Long count);

    public Promise<BElement, Exception> zrevrangeWithScores(byte[] key, long start, long stop);

    public Promise<BElement, Exception> zrevrank(byte[] key, byte[] member);

    public Promise<BElement, Exception> zscan(byte[] key, String cursor, Long count, String match);

    public Promise<BElement, Exception> zscore(byte[] key, byte[] member);

    public Promise<BElement, Exception> zunionstore(byte[] destination, String aggregate, List<Double> weights, byte[]... keys);

}
