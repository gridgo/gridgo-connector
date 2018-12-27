package io.gridgo.connector.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SETBIT)
public class RedisSetBitHandler extends AbstractRedisCommandHandler {

    public RedisSetBitHandler() {
        super("key", "offset", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BElement[] params) {
        return redis.setbit(params[0].asValue().getRaw(), params[1].asValue().getLong(), params[2].asValue().getInteger());
    }
}
