package io.gridgo.redis.command.geo;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.GEODIST)
public class RedisGeoDistHandler extends AbstractRedisCommandHandler {

    public RedisGeoDistHandler() {
        super("key", "from", "to");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        String unit = options.getString("unit", "m");
        return redis.geodist(params[0].asValue().getRaw(), params[1].asValue().getRaw(), params[2].asValue().getRaw(), unit);
    }

}
