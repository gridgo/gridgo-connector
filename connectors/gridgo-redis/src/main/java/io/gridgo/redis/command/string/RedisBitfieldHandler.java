package io.gridgo.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.BITFIELD)
public class RedisBitfieldHandler extends AbstractRedisCommandHandler {

    public RedisBitfieldHandler() {
        super("key", "args");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        String overflow = options.getString("overflow", null);
        Object[] args = new Object[params.length - 1];
        for (int i = 1; i < params.length; i++) {
            args[i - 1] = params[i].asValue().getData();
        }
        return redis.bitfield(params[0].asValue().getRaw(), overflow, args);
    }
}
