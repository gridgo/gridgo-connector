package io.gridgo.connector.redis.command.sortedset;

import java.util.function.Consumer;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZRANGEBYSCORE)
public class RedisZrangeByScoreHandler extends AbstractRedisCommandHandler {

    public RedisZrangeByScoreHandler() {
        super("key", "lower", "upper");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {

        BReference valueConsumerRef = options.getReference("valueConsumer", null);
        Consumer<byte[]> channel = valueConsumerRef == null ? null : (Consumer<byte[]>) valueConsumerRef.getReference();

        boolean includeLower = options.getBoolean("includeLower", false);
        boolean includeUpper = options.getBoolean("includeUpper", false);
        Long offset = options.getLong("offset", null);
        Long count = options.getLong("count", null);

        return redis.zrangebyscore(channel, params[0].asValue().getRaw(), includeLower, params[1].asValue().getLong(), params[2].asValue().getLong(),
                includeUpper, offset, count);
    }
}