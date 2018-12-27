package io.gridgo.connector.redis.command.hash;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.HSET)
public class RedisHSetHandler extends AbstractRedisCommandHandler {

    public RedisHSetHandler() {
        super("key", "field", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BElement[] params) {
        return redis.hset(params[0].asValue().getRaw(), params[1].asValue().getRaw(), params[2].asValue().getRaw());
    }
}
