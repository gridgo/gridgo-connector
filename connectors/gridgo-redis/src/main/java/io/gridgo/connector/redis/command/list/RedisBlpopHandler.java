package io.gridgo.connector.redis.command.list;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.BLPOP)
public class RedisBlpopHandler extends AbstractRedisCommandHandler {

    public RedisBlpopHandler() {
        super("timeout", "keys");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.blpop(params[0].asValue().getLong(), extractListBytesFromSecond(params));
    }

}
