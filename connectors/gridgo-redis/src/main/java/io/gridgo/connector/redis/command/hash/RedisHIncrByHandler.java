package io.gridgo.connector.redis.command.hash;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.HINCRBY)
public class RedisHIncrByHandler extends AbstractRedisCommandHandler {

    public RedisHIncrByHandler() {
        super("key", "field", "amount");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BElement[] params) {
        return redis.hincrby(params[0].asValue().getRaw(), params[1].asValue().getRaw(), params[2].asValue().getLong());
    }

}
