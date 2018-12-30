package io.gridgo.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZPOPMIN)
public class RedisZpopMinHandler extends AbstractRedisCommandHandler {

    public RedisZpopMinHandler() {
        super("key", "count");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        if (params.length == 1) {
            return redis.zpopmin(params[0].asValue().getRaw());
        }
        return redis.zpopmin(params[0].asValue().getRaw(), params[1].asValue().getLong());
    }
}
