package io.gridgo.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.BITPOS)
public class RedisBitposHandler extends AbstractRedisCommandHandler {

    public RedisBitposHandler() {
        super("key", "state", "start", "end");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.bitpos(params[0].asValue().getRaw(), params[1].asValue().getBoolean(), params[2].asValue().getLong(), params[3].asValue().getLong());
    }
}
