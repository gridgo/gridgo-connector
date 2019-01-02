package io.gridgo.redis.command.hash;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.HSCAN)
public class RedisHScanHandler extends AbstractRedisCommandHandler {

    public RedisHScanHandler() {
        super("key");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        final String cursor = options.getString("cursor", null);
        final String match = options.getString("match", null);
        final Long count = options.getLong("limit", options.getLong("count", null));
        return redis.hscan(params[0].asValue().getRaw(), cursor, count, match);
    }
}
