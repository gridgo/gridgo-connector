package io.gridgo.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.BITCOUNT)
public class RedisBitcountHandler extends AbstractRedisCommandHandler {

    public RedisBitcountHandler() {
        super("key");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        long start = 0;
        long end = 0;
        if (params.length > 1) {
            start = params[1].asValue().getLong();
            end = params[2].asValue().getLong();
        }
        return redis.bitcount(params[0].asValue().getRaw(), start, end);
    }
}
