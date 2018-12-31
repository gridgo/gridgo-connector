package io.gridgo.redis.lettuce.delegate;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.command.RedisGeoCommands;
import io.lettuce.core.GeoArgs;
import io.lettuce.core.GeoArgs.Sort;
import io.lettuce.core.GeoArgs.Unit;
import io.lettuce.core.GeoCoordinates;
import io.lettuce.core.GeoRadiusStoreArgs;
import io.lettuce.core.GeoWithin;
import io.lettuce.core.api.async.RedisGeoAsyncCommands;
import lombok.NonNull;

public interface LettuceGeoCommandsDelegate extends LettuceCommandsDelegate, RedisGeoCommands {

    default GeoArgs buildGeoArgs(boolean withdistance, boolean withcoordinates, boolean withHash, String sort, Long count) {
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

    default GeoRadiusStoreArgs<byte[]> buildGeoRadiusStoreArgs(byte[] storeKey, byte[] storeDistKey, Long count, String sort) {
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

    @Override
    default Promise<BElement, Exception> geoadd(byte[] key, double longitude, double latitude, byte[] member) {
        return toPromise(getGeoCommands().geoadd(key, longitude, latitude, member));
    }

    @Override
    default Promise<BElement, Exception> geoadd(byte[] key, Object... lngLatMember) {
        return toPromise(getGeoCommands().geoadd(key, lngLatMember));
    }

    default BArray geoCoordinatesToBArray(@NonNull GeoCoordinates coordinates) {
        return BArray.ofSequence(coordinates.getX(), coordinates.getY());
    }

    @Override
    default Promise<BElement, Exception> geodist(byte[] key, byte[] from, byte[] to, String unitStr) {
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(getGeoCommands().geodist(key, from, to, unit));
    }

    @Override
    default Promise<BElement, Exception> geohash(byte[] key, byte[]... members) {
        return toPromise(getGeoCommands().geohash(key, members) //
                                         .thenApply(list -> this.convertList(list, value -> value.getValue())));
    }

    @Override
    default Promise<BElement, Exception> geopos(byte[] key, byte[]... members) {
        return toPromise(getGeoCommands().geopos(key, members) //
                                         .thenApply(list -> this.convertList(list, this::geoCoordinatesToBArray)));
    }

    @Override
    default Promise<BElement, Exception> georadius(byte[] key, double longitude, double latitude, double distance, String unitStr, boolean withdistance,
            boolean withcoordinates, boolean withHash, Long count, String sort) {
        GeoArgs geoArgs = buildGeoArgs(withdistance, withcoordinates, withHash, sort, count);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(getGeoCommands().georadius(key, longitude, latitude, distance, unit, geoArgs) //
                                         .thenApply(list -> this.convertList(list, this::geoWithinToBObject)));
    }

    @Override
    default Promise<BElement, Exception> georadius(byte[] key, double longitude, double latitude, double distance, @NonNull String unitStr, byte[] storeKey,
            byte[] storeDistKey, Long count, String sort) {
        GeoRadiusStoreArgs<byte[]> args = buildGeoRadiusStoreArgs(storeKey, storeDistKey, count, sort);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(getGeoCommands().georadius(key, longitude, latitude, distance, unit, args));
    }

    @Override
    default Promise<BElement, Exception> georadiusbymember(byte[] key, byte[] member, double distance, String unitStr, boolean withdistance,
            boolean withcoordinates, boolean withHash, Long count, String sort) {
        GeoArgs geoArgs = buildGeoArgs(withdistance, withcoordinates, withHash, sort, count);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(getGeoCommands().georadiusbymember(key, member, distance, unit, geoArgs) //
                                         .thenApply(list -> this.convertList(list, this::geoWithinToBObject)));
    }

    @Override
    default Promise<BElement, Exception> georadiusbymember(byte[] key, byte[] member, double distance, @NonNull String unitStr, byte[] storeKey,
            byte[] storeDistKey, Long count, String sort) {
        GeoRadiusStoreArgs<byte[]> geoStoreArgs = buildGeoRadiusStoreArgs(storeKey, storeDistKey, count, sort);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(getGeoCommands().georadiusbymember(key, member, distance, unit, geoStoreArgs));
    }

    default BObject geoWithinToBObject(@NonNull GeoWithin<byte[]> geoWithin) {
        GeoCoordinates coordinates = geoWithin.getCoordinates();
        return BObject.ofSequence(//
                "coordinates", geoCoordinatesToBArray(coordinates) //
                , "distance", geoWithin.getDistance() //
                , "geoHash", geoWithin.getGeohash() //
                , "member", geoWithin.getMember());
    }

    <T extends RedisGeoAsyncCommands<byte[], byte[]>> T getGeoCommands();

}
