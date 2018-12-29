package io.gridgo.connector.redis.command.geo;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.GEOPOS)
public class RedisGeoPosHandler extends AbstractRedisCommandHandler {

    public RedisGeoPosHandler() {
        super("key", "member");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.geopos(params[0].asValue().getRaw(), extractListBytesFromSecond(params));
    }

}
