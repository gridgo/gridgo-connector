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

@RedisCommand(RedisCommands.ZREVRANGE)
public class RedisZrevRangeHandler extends AbstractRedisCommandHandler {

    public RedisZrevRangeHandler() {
        super("key", "start", "stop");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        BReference valueConsumerRef = options.getReference("valueConsumer", null);
        Consumer<byte[]> channel = valueConsumerRef == null ? null : (Consumer<byte[]>) valueConsumerRef.getReference();
        return redis.zrevrange(channel, params[0].asValue().getRaw(), params[1].asValue().getLong(), params[2].asValue().getLong());
    }
}
