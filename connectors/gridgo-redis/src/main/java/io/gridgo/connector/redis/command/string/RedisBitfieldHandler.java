package io.gridgo.connector.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;
import io.lettuce.core.BitFieldArgs;

@RedisCommand(RedisCommands.BITFIELD)
public class RedisBitfieldHandler extends AbstractRedisCommandHandler {

    public RedisBitfieldHandler() {
        super("key", "args");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BElement[] params) {
        return redis.bitfield(params[0].asValue().getRaw(), (BitFieldArgs) params[1].asReference().getReference());
    }
}
