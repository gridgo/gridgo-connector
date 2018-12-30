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

@RedisCommand(RedisCommands.ZRANGE)
public class RedisZrangeHandler extends AbstractRedisCommandHandler {

    public RedisZrangeHandler() {
        super("key", "start", "stop");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        BReference valueConsumerRef = options.getReference("valueConsumer", null);
        if (valueConsumerRef != null) {
            return redis.zrange((Consumer<byte[]>) valueConsumerRef.getReference(), params[0].asValue().getRaw(), params[1].asValue().getLong(),
                    params[2].asValue().getLong());
        }
        return redis.zrange(params[0].asValue().getRaw(), params[1].asValue().getLong(), params[2].asValue().getLong());
    }
}
