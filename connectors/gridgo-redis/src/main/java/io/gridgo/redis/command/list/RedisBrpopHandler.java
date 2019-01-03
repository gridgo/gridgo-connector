package io.gridgo.redis.command.list;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.BRPOP)
public class RedisBrpopHandler extends AbstractRedisCommandHandler {

    public RedisBrpopHandler() {
        super("timeout", "keys");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.brpop(params[0].asValue().getLong(), extractListBytesFromSecond(params));
    }

}
