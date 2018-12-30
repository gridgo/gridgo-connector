package io.gridgo.redis.command.list;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.LINSERT)
public class RedisLinsertHandler extends AbstractRedisCommandHandler {

    public RedisLinsertHandler() {
        super("key", "before", "pivot", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.linsert(params[0].asValue().getRaw(), params[1].asValue().getBoolean(), params[2].asValue().getRaw(), params[3].asValue().getRaw());
    }
}
