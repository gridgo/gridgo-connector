package io.gridgo.connector.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZPOPMAX)
public class RedisZpopMaxHandler extends AbstractRedisCommandHandler {

    public RedisZpopMaxHandler() {
        super("key", "count");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        if (params.length == 1) {
            return redis.zpopmax(params[0].asValue().getRaw());
        }
        return redis.zpopmax(params[0].asValue().getRaw(), params[1].asValue().getLong());
    }

}