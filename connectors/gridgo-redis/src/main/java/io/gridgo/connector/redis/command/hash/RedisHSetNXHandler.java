package io.gridgo.connector.redis.command.hash;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.HSETNX)
public class RedisHSetNXHandler extends AbstractRedisCommandHandler {

    public RedisHSetNXHandler() {
        super("key", "field", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.hsetnx(params[0].asValue().getRaw(), params[1].asValue().getRaw(), params[2].asValue().getRaw());
    }
}
