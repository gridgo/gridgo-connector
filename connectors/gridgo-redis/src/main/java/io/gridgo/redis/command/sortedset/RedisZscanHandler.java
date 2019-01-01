package io.gridgo.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZSCAN)
public class RedisZscanHandler extends AbstractRedisCommandHandler {

    public RedisZscanHandler() {
        super("key");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        final String cursor = options.getString("cursor", null);
        final String match = options.getString("match", null);
        final Long count = options.getLong("limit", options.getLong("count", null));
        return redis.zscan(params[0].asValue().getRaw(), cursor, count, match);
    }
}
