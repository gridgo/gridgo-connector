package io.gridgo.connector.redis.command.list;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.BRPOPLPUSH)
public class RedisBrpopPlusHandler extends AbstractRedisCommandHandler {

    public RedisBrpopPlusHandler() {
        super("timeout", "source", "destination");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.brpoplpush(params[0].asValue().getLong(), params[1].asValue().getRaw(), params[2].asValue().getRaw());
    }

}
