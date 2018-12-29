package io.gridgo.connector.redis.command.geo;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.GEOADD)
public class RedisGeoAddHandler extends AbstractRedisCommandHandler {

    public RedisGeoAddHandler() {
        super("key", "longitude", "latitude", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.geoadd(params[0].asValue().getRaw(), params[1].asValue().getDouble(), params[2].asValue().getDouble(), params[3].asValue().getRaw());
    }

}
