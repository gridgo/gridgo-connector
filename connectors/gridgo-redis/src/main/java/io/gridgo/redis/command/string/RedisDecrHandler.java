package io.gridgo.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.adapter.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.DECR)
public class RedisDecrHandler extends AbstractRedisCommandHandler {

    public RedisDecrHandler() {
        super("key");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.decr(params[0].asValue().getRaw());
    }
}
