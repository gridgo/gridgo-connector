package io.gridgo.connector.redis.command.hyperloglog;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.PFCOUNT)
public class RedisPfcountHandler extends AbstractRedisCommandHandler {

    public RedisPfcountHandler() {
        super("key");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.pfcount(extractListBytesFromFirst(params));
    }
}
