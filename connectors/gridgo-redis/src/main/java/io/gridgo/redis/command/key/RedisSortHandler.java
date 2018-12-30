package io.gridgo.redis.command.key;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SORT)
public class RedisSortHandler extends AbstractRedisCommandHandler {

    public RedisSortHandler() {
        super("key", "byPattern", "getPattern");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {

        Long count = options.getLong("count", options.getLong("limit", null));
        Long offset = options.getLong("offset", null);
        String order = options.getString("order", null);
        boolean alpha = options.getBoolean("alpha", false);
        Consumer<byte[]> channel = options.getReference("valueConsumer").getReference();

        List<String> getPatterns = new ArrayList<>();
        for (int i = 2; i < params.length; i++) {
            getPatterns.add(params[i].asValue().getString());
        }
        return redis.sort(channel, params[0].asValue().getRaw(), params[1].asValue().getString(), getPatterns, count, offset, order, alpha);
    }

}
