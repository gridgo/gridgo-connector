package io.gridgo.connector.redis.command.set;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SDIFFSTORE)
public class RedisSdiffstoreHandler extends AbstractRedisCommandHandler {

    public RedisSdiffstoreHandler() {
        super("destination", "keys");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.sdiffstore(params[0].asValue().getRaw(), extractListBytesFromSecond(params));
    }

}
