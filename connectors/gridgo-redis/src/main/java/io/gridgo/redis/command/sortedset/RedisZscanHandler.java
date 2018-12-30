package io.gridgo.redis.command.sortedset;

import java.util.function.BiConsumer;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.adapter.RedisClient;
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
        final BiConsumer<Double, byte[]> channel = options.getReference("consumer").getReference();
        final String cursor = options.getString("cursor", null);
        final String match = options.getString("match", null);
        final Long count = options.getLong("limit", null);
        return redis.zscan(channel, params[0].asValue().getRaw(), cursor, match, count);
    }
}
