package io.gridgo.connector.redis.command.set;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SMOVE)
public class RedisSmoveHandler extends AbstractRedisCommandHandler {

    public RedisSmoveHandler() {
        super("source", "destination", "member");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.smove(params[0].asValue().getRaw(), params[1].asValue().getRaw(), params[2].asValue().getRaw());
    }
}
