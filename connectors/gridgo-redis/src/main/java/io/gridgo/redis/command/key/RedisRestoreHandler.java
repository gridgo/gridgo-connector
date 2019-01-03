package io.gridgo.redis.command.key;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.RESTORE)
public class RedisRestoreHandler extends AbstractRedisCommandHandler {

    public RedisRestoreHandler() {
        super("key", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        long ttl = options.getLong("ttl");
        boolean replace = options.getBoolean("replace", false);
        return redis.restore(params[0].asValue().getRaw(), params[1].asValue().getRaw(), ttl, replace);
    }

}
