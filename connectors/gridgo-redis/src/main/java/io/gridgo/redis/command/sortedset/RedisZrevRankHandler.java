package io.gridgo.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.adapter.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZREVRANK)
public class RedisZrevRankHandler extends AbstractRedisCommandHandler {

    public RedisZrevRankHandler() {
        super("key", "member");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.zrevrank(params[0].asValue().getRaw(), params[1].asValue().getRaw());
    }
}
