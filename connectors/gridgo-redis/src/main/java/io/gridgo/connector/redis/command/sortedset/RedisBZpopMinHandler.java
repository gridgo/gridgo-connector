package io.gridgo.connector.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.BZPOPMIN)
public class RedisBZpopMinHandler extends AbstractRedisCommandHandler {

    public RedisBZpopMinHandler() {
        super("timeout", "key");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.bzpopmin(params[0].asValue().getLong(), extractListBytesFromSecond(params));
    }

}
