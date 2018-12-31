package io.gridgo.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisGeoCommands {

    public Promise<BElement, Exception> geoadd(byte[] key, double longitude, double latitude, byte[] member);

    public Promise<BElement, Exception> geoadd(byte[] key, Object... lngLatMember);

    public Promise<BElement, Exception> geodist(byte[] key, byte[] from, byte[] to, String unit);

    public Promise<BElement, Exception> geohash(byte[] key, byte[]... members);

    public Promise<BElement, Exception> geopos(byte[] key, byte[]... members);

    public Promise<BElement, Exception> georadius(byte[] key, double longitude, double latitude, double distance, String unit, boolean withdistance,
            boolean withcoordinates, boolean withHash, Long count, String sort);

    public Promise<BElement, Exception> georadius(byte[] key, double longitude, double latitude, double distance, String unit, byte[] storeKey,
            byte[] storeDistKey, Long count, String sort);

    public Promise<BElement, Exception> georadiusbymember(byte[] key, byte[] member, double distance, String unitStr, boolean withdistance,
            boolean withcoordinates, boolean withHash, Long count, String sort);

    public Promise<BElement, Exception> georadiusbymember(byte[] key, byte[] member, double distance, String unit, byte[] storeKey, byte[] storeDistKey,
            Long count, String sort);

}
