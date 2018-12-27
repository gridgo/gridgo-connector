package io.gridgo.connector.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.MGET)
public class RedisMGetHandler extends AbstractRedisCommandHandler {

    public RedisMGetHandler() {
        super("key");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BElement[] params) {
        byte[][] keys = new byte[params.length][];
        for (int i = 0; i < params.length; i++) {
            keys[i] = params[i].asValue().getRaw();
        }
        return redis.mget(keys);
    }
}
