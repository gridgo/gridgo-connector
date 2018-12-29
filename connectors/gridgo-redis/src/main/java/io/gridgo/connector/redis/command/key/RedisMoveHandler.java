package io.gridgo.connector.redis.command.key;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.MOVE)
public class RedisMoveHandler extends AbstractRedisCommandHandler {

    public RedisMoveHandler() {
        super("key", "db");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.move(params[0].asValue().getRaw(), params[1].asValue().getInteger());
    }

}
