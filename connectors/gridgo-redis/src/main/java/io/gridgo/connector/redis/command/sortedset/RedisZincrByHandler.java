package io.gridgo.connector.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZINCRBY)
public class RedisZincrByHandler extends AbstractRedisCommandHandler {

    public RedisZincrByHandler() {
        super("key", "amount", "member");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.zincrby(params[0].asValue().getRaw(), params[1].asValue().getDouble(), params[2].asValue().getRaw());
    }
}