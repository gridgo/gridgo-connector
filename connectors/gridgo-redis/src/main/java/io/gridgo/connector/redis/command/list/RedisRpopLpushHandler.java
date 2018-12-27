package io.gridgo.connector.redis.command.list;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.RPOPLPUSH)
public class RedisRpopLpushHandler extends AbstractRedisCommandHandler {

    public RedisRpopLpushHandler() {
        super("source", "destination");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BElement[] params) {
        return redis.rpoplpush(params[0].asValue().getRaw(), params[1].asValue().getRaw());
    }
}
