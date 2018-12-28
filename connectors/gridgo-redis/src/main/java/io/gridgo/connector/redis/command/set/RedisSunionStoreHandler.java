package io.gridgo.connector.redis.command.set;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

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
