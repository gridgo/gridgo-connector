package io.gridgo.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SETEX)
public class RedisSetEXHandler extends AbstractRedisCommandHandler {

    public RedisSetEXHandler() {
        super("key", "duration", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.setex(params[0].asValue().getRaw(), params[1].asValue().getLong(), params[2].asValue().getRaw());
    }
}
