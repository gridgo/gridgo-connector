package io.gridgo.redis.command.set;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SUNIONSTORE)
public class RedisSunionStoreHandler extends AbstractRedisCommandHandler {

    public RedisSunionStoreHandler() {
        super("destination", "key");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.sunionstore(params[0].asValue().getRaw(), extractListBytesFromSecond(params));
    }

}
