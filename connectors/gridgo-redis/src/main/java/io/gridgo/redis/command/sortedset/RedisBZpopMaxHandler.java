package io.gridgo.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.BZPOPMAX)
public class RedisBZpopMaxHandler extends AbstractRedisCommandHandler {

    public RedisBZpopMaxHandler() {
        super("timeout", "key");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.bzpopmax(params[0].asValue().getLong(), extractListBytesFromSecond(params));
    }

}
