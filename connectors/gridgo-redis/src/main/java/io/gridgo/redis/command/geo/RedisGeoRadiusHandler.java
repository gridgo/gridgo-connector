package io.gridgo.redis.command.geo;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.GEORADIUS)
public class RedisGeoRadiusHandler extends AbstractRedisCommandHandler {

    public RedisGeoRadiusHandler() {
        super("key", "longitude", "latitude", "distance");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {

        byte[] storeKey = options.getRaw("store", options.getRaw("storeKey", null));
        byte[] storeDistKey = options.getRaw("storeDist", options.getRaw("storeDistKey", null));
        Long count = options.getLong("count", options.getLong("limit", null));
        String sort = options.getString("sort", null);

        String unit = options.getString("unit", "m");

        return redis.georadius(params[0].asValue().getRaw() //
                , params[1].asValue().getDouble() //
                , params[2].asValue().getDouble() //
                , params[3].asValue().getDouble() //
                , unit //
                , storeKey //
                , storeDistKey //
                , count //
                , sort //
        );
    }

}
