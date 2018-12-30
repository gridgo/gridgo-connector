package io.gridgo.redis.command.key;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.PEXPIREAT)
public class RedisPexpireatHandler extends AbstractRedisCommandHandler {

    public RedisPexpireatHandler() {
        super("key", "timestamp");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.pexpireat(params[0].asValue().getRaw(), params[1].asValue().getLong());
    }

}
