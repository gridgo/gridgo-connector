package io.gridgo.redis.command.set;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SSCAN)
public class RedisSscanHandler extends AbstractRedisCommandHandler {

    public RedisSscanHandler() {
        super("key");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        final String cursor = options.getString("cursor", null);
        final String match = options.getString("match", null);
        final Long count = options.getLong("limit", options.getLong("count", null));
        return redis.sscan(params[0].asValue().getRaw(), cursor, count, match);
    }

}
