package io.gridgo.connector.redis.command.key;

import java.util.function.Consumer;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SCAN)
public class RedisScaHandler extends AbstractRedisCommandHandler {

    public RedisScaHandler() {
        super("cursor");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        Consumer<byte[]> channel = options.getReference("channel").getReference();
        String cursor = params[0].asValue().getString();
        Long count = options.getLong("count", options.getLong("limit", null));
        String match = options.getString("match", null);

        return redis.scan(channel, cursor, count, match);
    }

}