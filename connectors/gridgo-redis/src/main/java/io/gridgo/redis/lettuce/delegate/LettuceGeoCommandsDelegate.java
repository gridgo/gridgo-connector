package io.gridgo.redis.lettuce.delegate;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.redis.adapter.RedisGeoCommands;
import io.lettuce.core.GeoArgs;
import io.lettuce.core.GeoRadiusStoreArgs;
import io.lettuce.core.GeoArgs.Unit;
import io.lettuce.core.api.async.RedisGeoAsyncCommands;
import lombok.NonNull;

public interface LettuceGeoCommandsDelegate extends LettuceCommandsDelegate, RedisGeoCommands {

    <T extends RedisGeoAsyncCommands<byte[], byte[]>> T getGeoCommands();

    @Override
    default Promise<BElement, Exception> geoadd(byte[] key, double longitude, double latitude, byte[] member) {
        return toPromise(getGeoCommands().geoadd(key, longitude, latitude, member));
    }

    @Override
    default Promise<BElement, Exception> geoadd(byte[] key, Object... lngLatMember) {
        return toPromise(getGeoCommands().geoadd(key, lngLatMember));
    }

    @Override
    default Promise<BElement, Exception> geohash(byte[] key, byte[]... members) {
        return toPromise(getGeoCommands().geohash(key, members));
    }

    @Override
    default Promise<BElement, Exception> georadiusbymember(byte[] key, byte[] member, double distance, @NonNull String unitStr, byte[] storeKey,
            byte[] storeDistKey, Long count, String sort) {
        GeoRadiusStoreArgs<byte[]> geoStoreArgs = buildGeoRadiusStoreArgs(storeKey, storeDistKey, count, sort);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(getGeoCommands().georadiusbymember(key, member, distance, unit, geoStoreArgs));
    }

    @Override
    default Promise<BElement, Exception> georadiusbymember(byte[] key, byte[] member, double distance, String unitStr, boolean withdistance,
            boolean withcoordinates, boolean withHash, Long count, String sort) {
        GeoArgs geoArgs = buildGeoArgs(withdistance, withcoordinates, withHash, sort, count);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(getGeoCommands().georadiusbymember(key, member, distance, unit, geoArgs));
    }

    @Override
    default Promise<BElement, Exception> georadius(byte[] key, double longitude, double latitude, double distance, @NonNull String unitStr, byte[] storeKey,
            byte[] storeDistKey, Long count, String sort) {
        GeoRadiusStoreArgs<byte[]> args = buildGeoRadiusStoreArgs(storeKey, storeDistKey, count, sort);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(getGeoCommands().georadius(key, longitude, latitude, distance, unit, args));
    }

    @Override
    default Promise<BElement, Exception> georadius(byte[] key, double longitude, double latitude, double distance, String unitStr, boolean withdistance,
            boolean withcoordinates, boolean withHash, Long count, String sort) {
        GeoArgs geoArgs = buildGeoArgs(withdistance, withcoordinates, withHash, sort, count);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(getGeoCommands().georadius(key, longitude, latitude, distance, unit, geoArgs));
    }

    @Override
    default Promise<BElement, Exception> geopos(byte[] key, byte[]... members) {
        return toPromise(getGeoCommands().geopos(key, members));
    }

    @Override
    default Promise<BElement, Exception> geodist(byte[] key, byte[] from, byte[] to, String unitStr) {
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(getGeoCommands().geodist(key, from, to, unit));
    }

}
