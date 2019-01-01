package io.gridgo.redis.lettuce.delegate;

import java.util.List;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.command.RedisSortedSetCommands;
import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.Range.Boundary;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.ScoredValueScanCursor;
import io.lettuce.core.ZAddArgs;
import io.lettuce.core.ZStoreArgs;
import io.lettuce.core.api.async.RedisSortedSetAsyncCommands;

public interface LettuceSortedSetCommandsDelegate extends LettuceCommandsDelegate, RedisSortedSetCommands, LettuceScannable {

    default Range<byte[]> buildRangeBytes(boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper) {
        Boundary<byte[]> lowerBoundary = includeLower ? Boundary.including(lower) : Boundary.excluding(lower);
        Boundary<byte[]> upperBoundary = includeUpper ? Boundary.including(upper) : Boundary.excluding(upper);
        return Range.from(lowerBoundary, upperBoundary);
    }

    default Range<Long> buildRangeLong(boolean includeLower, long lower, long upper, boolean includeUpper) {
        Boundary<Long> lowerBoundary = includeLower ? Boundary.including(lower) : Boundary.excluding(lower);
        Boundary<Long> upperBoundary = includeUpper ? Boundary.including(upper) : Boundary.excluding(upper);
        return Range.from(lowerBoundary, upperBoundary);
    }

    default ZStoreArgs buildZStoreArgs(String aggregate, List<Double> weights) {
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
        return args;
    }

    @Override
    default Promise<BElement, Exception> bzpopmax(long timeout, byte[]... keys) {
        return toPromise(getSortedSetCommands().bzpopmax(timeout, keys) //
                                               .thenApply(keyValue -> BArray.ofSequence(keyValue.getKey(), keyValue.getValue().getValue(),
                                                       keyValue.getValue().getScore())));
    }

    @Override
    default Promise<BElement, Exception> bzpopmin(long timeout, byte[]... keys) {
        return toPromise(getSortedSetCommands().bzpopmin(timeout, keys) //
                                               .thenApply(keyValue -> BArray.ofSequence(keyValue.getKey(), keyValue.getValue().getValue(),
                                                       keyValue.getValue().getScore())));
    }

    <T extends RedisSortedSetAsyncCommands<byte[], byte[]>> T getSortedSetCommands();

    default BArray scoredValueToBArray(ScoredValue<byte[]> scoredValue) {
        return BArray.ofSequence(scoredValue.getValue(), scoredValue.getScore());
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
    default Promise<BElement, Exception> zadd(byte[] key, double score, byte[] member) {
        return toPromise(getSortedSetCommands().zadd(key, score, member));
    }

    @Override
    default Promise<BElement, Exception> zadd(byte[] key, Object... scoresAndValues) {
        return toPromise(getSortedSetCommands().zadd(key, scoresAndValues));
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
    default Promise<BElement, Exception> zpopmax(byte[] key) {
        return toPromise(getSortedSetCommands().zpopmax(key) //
                                               .thenApply(this::scoredValueToBArray));
    }

    @Override
    default Promise<BElement, Exception> zpopmax(byte[] key, long count) {
        return toPromise(getSortedSetCommands().zpopmax(key, count) //
                                               .thenApply(list -> this.convertList(list, this::scoredValueToBArray)));
    }

    @Override
    default Promise<BElement, Exception> zpopmin(byte[] key) {
        return toPromise(getSortedSetCommands().zpopmin(key)//
                                               .thenApply(this::scoredValueToBArray));
    }

    @Override
    default Promise<BElement, Exception> zpopmin(byte[] key, long count) {
        return toPromise(getSortedSetCommands().zpopmin(key, count)//
                                               .thenApply(list -> this.convertList(list, this::scoredValueToBArray)));
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
    default Promise<BElement, Exception> zrangeWithScores(byte[] key, long start, long stop) {
        return toPromise(getSortedSetCommands().zrangeWithScores(key, start, stop) //
                                               .thenApply(list -> this.convertList(list, this::scoredValueToBArray)));
    }

    @Override
    default Promise<BElement, Exception> zrank(byte[] key, byte[] member) {
        return toPromise(getSortedSetCommands().zrank(key, member));
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
    default Promise<BElement, Exception> zrevrangeWithScores(byte[] key, long start, long stop) {
        return toPromise(getSortedSetCommands().zrevrangeWithScores(key, start, stop) //
                                               .thenApply(list -> this.convertList(list, this::scoredValueToBArray)));
    }

    @Override
    default Promise<BElement, Exception> zrevrank(byte[] key, byte[] member) {
        return toPromise(getSortedSetCommands().zrevrank(key, member));
    }

    @Override
    default Promise<BElement, Exception> zscan(byte[] key, String cursor, Long count, String match) {
        ScanCursor scanCursor = cursor == null ? null : new ScanCursor();
        ScanArgs scanArgs = buildScanArgs(count, match);

        final RedisFuture<ScoredValueScanCursor<byte[]>> future;

        if (cursor != null) {
            scanCursor.setCursor(cursor);
            if (scanArgs == null) {
                future = getSortedSetCommands().zscan(key, scanCursor);
            } else {
                future = getSortedSetCommands().zscan(key, scanCursor, scanArgs);
            }
        } else {
            if (scanArgs == null) {
                future = getSortedSetCommands().zscan(key);
            } else {
                future = getSortedSetCommands().zscan(key, scanArgs);
            }
        }

        return toPromise(future.thenApply(scoredValueScanCursor -> BObject.ofSequence( //
                "cursor", scoredValueScanCursor.getCursor(), //
                "values", this.convertList(scoredValueScanCursor.getValues(), this::scoredValueToBArray))));
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
