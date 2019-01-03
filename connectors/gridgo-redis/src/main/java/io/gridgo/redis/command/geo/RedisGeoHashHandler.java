package io.gridgo.redis.command.geo;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.GEOHASH)
public class RedisGeoHashHandler extends AbstractRedisCommandHandler {

    public RedisGeoHashHandler() {
        super("key", "member");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.geohash(params[0].asValue().getRaw(), extractListBytesFromSecond(params));
    }

}
