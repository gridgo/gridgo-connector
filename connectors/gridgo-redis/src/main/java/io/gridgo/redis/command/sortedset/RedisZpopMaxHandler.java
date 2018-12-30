package io.gridgo.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.adapter.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

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
