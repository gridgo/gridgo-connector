package io.gridgo.connector.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZREMRANGEBYSCORE)
public class RedisZremRangeByScoreHandler extends AbstractRedisCommandHandler {

    public RedisZremRangeByScoreHandler() {
        super("key", "start", "stop");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        boolean includeLower = options.getBoolean("includeLower", false);
        boolean includeUpper = options.getBoolean("includeUpper", false);
        return redis.zremrangebyscore(params[0].asValue().getRaw(), includeLower, params[1].asValue().getLong(), params[2].asValue().getLong(), includeUpper);
    }
}
