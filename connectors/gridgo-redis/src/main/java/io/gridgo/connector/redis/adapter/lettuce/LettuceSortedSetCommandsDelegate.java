package io.gridgo.connector.redis.adapter.lettuce;

import java.util.List;
import java.util.function.BiConsumer;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisSortedSetCommands;
import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.StreamScanCursor;
import io.lettuce.core.ZAddArgs;
import io.lettuce.core.ZStoreArgs;
import io.lettuce.core.api.async.RedisSortedSetAsyncCommands;
import io.lettuce.core.output.ScoredValueStreamingChannel;
import lombok.NonNull;

public interface LettuceSortedSetCommandsDelegate extends LettuceCommandsDelegate, RedisSortedSetCommands {

    <T extends RedisSortedSetAsyncCommands<byte[], byte[]>> T getSortedSetCommands();

    @Override
    default Promise<BElement, Exception> bzpopmin(long timeout, byte[]... keys) {
        return toPromise(getSortedSetCommands().bzpopmin(timeout, keys));
    }

    @Override
    default Promise<BElement, Exception> bzpopmax(long timeout, byte[]... keys) {
        return toPromise(getSortedSetCommands().bzpopmax(timeout, keys));
    }

    @Override
    default Promise<BElement, Exception> zadd(byte[] key, Object... scoresAndValues) {
        return toPromise(getSortedSetCommands().zadd(key, scoresAndValues));
    }

    @Override
    default Promise<BElement, Exception> zadd(byte[] key, double score, byte[] member) {
        return toPromise(getSortedSetCommands().zadd(key, score, member));
    }

    @Override
    default Promise<BElement, Exception> zadd(byte[] key, boolean xx, boolean nx, boolean ch, Object... scoresAndValues) {
        ZAddArgs args = new ZAddArgs();
        if (xx)
            args.xx();
        if (nx)
            args.nx();
        if (ch)
            args.ch();
        return toPromise(getSortedSetCommands().zadd(key, args, scoresAndValues));
    }

    @Override
    default Promise<BElement, Exception> zcard(byte[] key) {
        return toPromise(getSortedSetCommands().zcard(key));
    }

    @Override
    default Promise<BElement, Exception> zcount(byte[] key, boolean includeLower, long lower, long upper, boolean includeUpper) {
        return toPromise(getSortedSetCommands().zcount(key, buildRangeLong(includeLower, lower, upper, includeUpper)));
    }

    @Override
    default Promise<BElement, Exception> zincrby(byte[] key, double amount, byte[] member) {
        return toPromise(getSortedSetCommands().zincrby(key, amount, member));
    }

    @Override
    default Promise<BElement, Exception> zinterstore(byte[] destination, String aggregate, List<Double> weights, byte[]... keys) {
        if (aggregate != null) {
            ZStoreArgs args;
            switch (aggregate.trim().toLowerCase()) {
            case "min":
                args = ZStoreArgs.Builder.min();
                break;
            case "max":
                args = ZStoreArgs.Builder.max();
                break;
            case "sum":
                args = ZStoreArgs.Builder.sum();
                break;
            default:
                throw new IllegalArgumentException("aggregate value should be null or one of [min | max | sum], got: " + aggregate);
            }

            if (weights != null) {
                double[] _weights = new double[weights.size()];
                for (int i = 0; i < _weights.length; i++) {
                    _weights[i] = weights.get(i);
                }
                args.weights(_weights);
            }
            return toPromise(getSortedSetCommands().zinterstore(destination, args, keys));
        }
        return toPromise(getSortedSetCommands().zinterstore(destination, keys));
    }

    @Override
    default Promise<BElement, Exception> zlexcount(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper) {
        Range<byte[]> range = buildRangeBytes(includeLower, lower, upper, includeUpper);
        return toPromise(getSortedSetCommands().zlexcount(key, range));
    }

    @Override
    default Promise<BElement, Exception> zpopmin(byte[] key, long count) {
        return toPromise(getSortedSetCommands().zpopmin(key, count));
    }

    @Override
    default Promise<BElement, Exception> zpopmin(byte[] key) {
        return toPromise(getSortedSetCommands().zpopmin(key));
    }

    @Override
    default Promise<BElement, Exception> zpopmax(byte[] key) {
        return toPromise(getSortedSetCommands().zpopmax(key));
    }

    @Override
    default Promise<BElement, Exception> zpopmax(byte[] key, long count) {
        return toPromise(getSortedSetCommands().zpopmax(key, count));
    }

    @Override
    default Promise<BElement, Exception> zrange(byte[] key, long start, long stop) {
        return toPromise(getSortedSetCommands().zrange(key, start, stop));
    }

    @Override
    default Promise<BElement, Exception> zrange(java.util.function.Consumer<byte[]> channel, byte[] key, long start, long stop) {
        return toPromise(getSortedSetCommands().zrange(bytes -> channel.accept(bytes), key, start, stop));
    }

    @Override
    default Promise<BElement, Exception> zrevrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper, Long offset,
            Long count) {
        Range<byte[]> range = buildRangeBytes(includeLower, lower, upper, includeUpper);
        if (offset != null && count != null) {
            Limit limit = Limit.create(offset, count);
            return toPromise(getSortedSetCommands().zrangebylex(key, range, limit));
        }
        return toPromise(getSortedSetCommands().zrevrangebylex(key, range));
    }

    @Override
    default Promise<BElement, Exception> zrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper, Long offset,
            Long count) {
        Range<byte[]> range = buildRangeBytes(includeLower, lower, upper, includeUpper);
        if (offset != null && count != null) {
            Limit limit = Limit.create(offset, count);
            return toPromise(getSortedSetCommands().zrangebylex(key, range, limit));
        }
        return toPromise(getSortedSetCommands().zrangebylex(key, range));
    }

    @Override
    default Promise<BElement, Exception> zrangebyscore(java.util.function.Consumer<byte[]> channel, byte[] key, boolean includeLower, long lower, long upper,
            boolean includeUpper, Long offset, Long count) {
        Range<Long> range = buildRangeLong(includeLower, lower, upper, includeUpper);
        if (offset != null && count != null) {
            Limit limit = Limit.create(offset, count);
            if (channel == null) {
                return toPromise(getSortedSetCommands().zrangebyscore(key, range, limit));
            } else {
                return toPromise(getSortedSetCommands().zrangebyscore(bytes -> channel.accept(bytes), key, range, limit));
            }
        }
        if (channel == null) {
            return toPromise(getSortedSetCommands().zrangebyscore(key, range));
        }
        return toPromise(getSortedSetCommands().zrangebyscore(bytes -> channel.accept(bytes), key, range));
    }

    @Override
    default Promise<BElement, Exception> zrank(byte[] key, byte[] member) {
        return toPromise(getSortedSetCommands().zrank(key, member));
    }

    @Override
    default Promise<BElement, Exception> zrangeWithScores(byte[] key, long start, long stop) {
        return toPromise(getSortedSetCommands().zrangeWithScores(key, start, stop));
    }

    @Override
    default Promise<BElement, Exception> zrem(byte[] key, byte[]... members) {
        return toPromise(getSortedSetCommands().zrem(key, members));
    }

    @Override
    default Promise<BElement, Exception> zremrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper) {
        return toPromise(getSortedSetCommands().zremrangebylex(key, buildRangeBytes(includeLower, lower, upper, includeUpper)));
    }

    @Override
    default Promise<BElement, Exception> zremrangebyrank(byte[] key, long start, long stop) {
        return toPromise(getSortedSetCommands().zremrangebyrank(key, start, stop));
    }

    @Override
    default Promise<BElement, Exception> zremrangebyscore(byte[] key, boolean includeLower, long lower, long upper, boolean includeUpper) {
        return toPromise(getSortedSetCommands().zremrangebyscore(key, buildRangeLong(includeLower, lower, upper, includeUpper)));
    }

    @Override
    default Promise<BElement, Exception> zrevrange(java.util.function.Consumer<byte[]> channel, byte[] key, long start, long stop) {
        if (channel == null) {
            return toPromise(getSortedSetCommands().zrevrange(key, start, stop));
        }
        return toPromise(getSortedSetCommands().zrevrange(bytes -> channel.accept(bytes), key, start, stop));
    }

    @Override
    default Promise<BElement, Exception> zrevrangeWithScores(byte[] key, long start, long stop) {
        return toPromise(getSortedSetCommands().zrevrangeWithScores(key, start, stop));
    }

    @Override
    default Promise<BElement, Exception> zrevrangebyscore(java.util.function.Consumer<byte[]> channel, byte[] key, boolean includeLower, long lower, long upper,
            boolean includeUpper, Long offset, Long count) {
        Range<Long> range = buildRangeLong(includeLower, lower, upper, includeUpper);
        if (offset != null && count != null) {
            Limit limit = Limit.create(offset, count);
            if (channel == null) {
                return toPromise(getSortedSetCommands().zrevrangebyscore(key, range, limit));
            } else {
                return toPromise(getSortedSetCommands().zrevrangebyscore(bytes -> channel.accept(bytes), key, range, limit));
            }
        }
        if (channel != null) {
            return toPromise(getSortedSetCommands().zrevrangebyscore(bytes -> channel.accept(bytes), key, range));
        }
        return toPromise(getSortedSetCommands().zrevrangebyscore(key, range));
    }

    @Override
    default Promise<BElement, Exception> zrevrank(byte[] key, byte[] member) {
        return toPromise(getSortedSetCommands().zrevrank(key, member));
    }

    @Override
    default Promise<BElement, Exception> zscan(@NonNull BiConsumer<Double, byte[]> consumer, byte[] key, String cursor, String match, Long limit) {

        ScanCursor scanCursor = cursor == null ? null : new ScanCursor();
        ScanArgs scanArgs = (match != null && limit != null) ? ScanArgs.Builder.limit(limit).match(match) : null;

        final RedisFuture<StreamScanCursor> future;

        ScoredValueStreamingChannel<byte[]> channel = scoredValue -> consumer.accept(scoredValue.getScore(), scoredValue.getValue());
        if (cursor != null) {
            scanCursor.setCursor(cursor);
            if (scanArgs == null) {
                future = getSortedSetCommands().zscan(channel, key, scanCursor);
            } else {
                future = getSortedSetCommands().zscan(channel, key, scanCursor, scanArgs);
            }
        } else {
            if (scanArgs == null) {
                future = getSortedSetCommands().zscan(channel, key);
            } else {
                future = getSortedSetCommands().zscan(channel, key, scanArgs);
            }
        }

        return toPromise(future).filterDone(ref -> {
            StreamScanCursor streamScanCursor = ref.asReference().getReference();
            return BObject.ofSequence("count", streamScanCursor.getCount(), "cursor", streamScanCursor.getCursor(), "finished", streamScanCursor.isFinished());
        });
    }

    @Override
    default Promise<BElement, Exception> zscore(byte[] key, byte[] member) {
        return toPromise(getSortedSetCommands().zscore(key, member));
    }

    @Override
    default Promise<BElement, Exception> zunionstore(byte[] destination, String aggregate, List<Double> weights, byte[]... keys) {
        ZStoreArgs storeArgs = null;
        if (aggregate != null) {
            storeArgs = buildZStoreArgs(aggregate, weights);
        }
        if (storeArgs != null) {
            return toPromise(getSortedSetCommands().zunionstore(destination, storeArgs, keys));
        }
        return toPromise(getSortedSetCommands().zunionstore(destination, keys));
    }
}
