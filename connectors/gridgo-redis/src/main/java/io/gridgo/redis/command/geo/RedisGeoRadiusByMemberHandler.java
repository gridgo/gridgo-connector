package io.gridgo.redis.command.geo;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.GEORADIUSBYMEMBER)
public class RedisGeoRadiusByMemberHandler extends AbstractRedisCommandHandler {

    public RedisGeoRadiusByMemberHandler() {
        super("key", "member", "distance");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {

        final Long count = options.getLong("count", options.getLong("limit", null));
        final String sort = options.getString("sort", null);
        final String unit = options.getString("unit", "m");
        byte[] storeKey = options.getRaw("store", options.getRaw("storeKey", null));
        byte[] storeDistKey = options.getRaw("storeDist", options.getRaw("storeDistKey", null));

        if (storeKey != null || storeDistKey != null) {
            return redis.georadiusbymember(params[0].asValue().getRaw() //
                    , params[1].asValue().getRaw() //
                    , params[2].asValue().getDouble() //
                    , unit //
                    , storeKey //
                    , storeDistKey //
                    , count //
                    , sort //
            );
        } else {
            boolean withDistance = options.getBoolean("withDistance", false);
            boolean withCoordinates = options.getBoolean("withCoordinates", false);
            boolean withHash = options.getBoolean("withHash", false);

            return redis.georadiusbymember(params[0].asValue().getRaw() //
                    , params[1].asValue().getRaw() //
                    , params[2].asValue().getDouble() //
                    , unit //
                    , withDistance //
                    , withCoordinates//
                    , withHash //
                    , count //
                    , sort //
            );
        }
    }

}
