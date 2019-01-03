package io.gridgo.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZREM)
public class RedisZremHandler extends AbstractRedisCommandHandler {

    public RedisZremHandler() {
        super("key", "member");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.zrem(params[0].asValue().getRaw(), extractListBytesFromSecond(params));
    }
}
