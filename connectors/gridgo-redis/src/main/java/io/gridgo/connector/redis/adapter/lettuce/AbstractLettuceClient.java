package io.gridgo.connector.redis.adapter.lettuce;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisConfig;
import io.gridgo.connector.redis.adapter.RedisType;
import io.gridgo.framework.AbstractComponentLifecycle;
import io.lettuce.core.GeoArgs;
import io.lettuce.core.GeoRadiusStoreArgs;
import io.lettuce.core.Range;
import io.lettuce.core.Range.Boundary;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.SortArgs;
import io.lettuce.core.ZStoreArgs;
import io.lettuce.core.GeoArgs.Sort;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractLettuceClient extends AbstractComponentLifecycle implements io.gridgo.connector.redis.adapter.RedisClient {

    @Getter(AccessLevel.PROTECTED)
    private final RedisType type;

    @Getter(AccessLevel.PROTECTED)
    private final RedisConfig config;

    @Getter(AccessLevel.PROTECTED)
    private RedisCodec<byte[], byte[]> codec = new ByteArrayCodec();

    private Function<Object, BElement> parser = this::parse;

    protected AbstractLettuceClient(@NonNull RedisType type, @NonNull RedisConfig config) {
        this.type = type;
        this.config = config;
        if (config.getParser() != null) {
            this.parser = config.getParser();
        }
    }

    private <T> BElement parse(T data) {
        return BElement.ofAny(data);
    }

    protected Promise<BElement, Exception> toPromise(RedisFuture<?> future) {
        return new CompletableDeferredObject<BElement, Exception>( //
                (CompletableFuture<BElement>) future.thenApply(this.parser) //
        ).promise();
    }

    @Override
    protected String generateName() {
        return type + "." + config.getAddress();
    }

    protected GeoRadiusStoreArgs<byte[]> buildGeoRadiusStoreArgs(byte[] storeKey, byte[] storeDistKey, Long count, String sort) {
        GeoRadiusStoreArgs<byte[]> args = (storeKey != null || storeDistKey != null || count != null || sort != null) //
                ? new GeoRadiusStoreArgs<byte[]>() //
                : null;

        if (storeKey != null) {
            args.withStore(storeKey);
        }
        if (storeDistKey != null) {
            args.withStoreDist(storeDistKey);
        }
        if (count != null) {
            args.withCount(count);
        }
        if (sort != null) {
            args.sort(Sort.valueOf(sort.trim().toLowerCase()));
        }
        return args;
    }

    protected GeoArgs buildGeoArgs(boolean withdistance, boolean withcoordinates, boolean withHash, String sort, Long count) {
        GeoArgs geoArgs = new GeoArgs();
        if (withdistance) {
            geoArgs.withDistance();
        }

        if (withcoordinates) {
            geoArgs.withCoordinates();
        }

        if (withHash) {
            geoArgs.withHash();
        }

        if (sort != null) {
            geoArgs.sort(Sort.valueOf(sort.trim().toLowerCase()));
        }

        if (count != null) {
            geoArgs.withCount(count);
        }
        return geoArgs;
    }

    protected ZStoreArgs buildZStoreArgs(String aggregate, List<Double> weights) {
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

    protected SortArgs buildSortArgs(String byPattern, List<String> getPatterns, Long count, Long offset, String order, boolean alpha) {
        SortArgs sortArgs = new SortArgs();
        sortArgs.by(byPattern);
        if (getPatterns != null) {
            getPatterns.forEach(pattern -> sortArgs.get(pattern));
        }
        if (count != null && offset != null) {
            sortArgs.limit(offset, count);
        }
        if (alpha) {
            sortArgs.alpha();
        }

        if (order != null) {
            switch (order.trim().toLowerCase()) {
            case "asc":
                sortArgs.asc();
                break;
            case "desc":
                sortArgs.desc();
                break;
            }
        }
        return sortArgs;
    }

    protected Range<Long> buildRangeLong(boolean includeLower, long lower, long upper, boolean includeUpper) {
        Boundary<Long> lowerBoundary = includeLower ? Boundary.including(lower) : Boundary.excluding(lower);
        Boundary<Long> upperBoundary = includeUpper ? Boundary.including(upper) : Boundary.excluding(upper);
        return Range.from(lowerBoundary, upperBoundary);
    }

    protected Range<byte[]> buildRangeBytes(boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper) {
        Boundary<byte[]> lowerBoundary = includeLower ? Boundary.including(lower) : Boundary.excluding(lower);
        Boundary<byte[]> upperBoundary = includeUpper ? Boundary.including(upper) : Boundary.excluding(upper);
        return Range.from(lowerBoundary, upperBoundary);
    }
}
