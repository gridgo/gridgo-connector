package io.gridgo.redis.command.sortedset;

import java.util.function.Consumer;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZREVRANGEBYSCORE)
public class RedisZrevRangeByScoreHandler extends AbstractRedisCommandHandler {

    public RedisZrevRangeByScoreHandler() {
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

        return redis.zrevrangebyscore(channel, params[0].asValue().getRaw(), includeLower, params[1].asValue().getLong(), params[2].asValue().getLong(),
                includeUpper, offset, count);
    }
}
