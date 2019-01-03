package io.gridgo.redis.command.key;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SCAN)
public class RedisScaHandler extends AbstractRedisCommandHandler {

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        final String cursor = options.getString("cursor", null);
        final String match = options.getString("match", null);
        final Long count = options.getLong("limit", options.getLong("count", null));
        return redis.scan(cursor, count, match);
    }

}
