package io.gridgo.redis.command.key;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.OBJECT)
public class RedisObjectHandler extends AbstractRedisCommandHandler {

    public RedisObjectHandler() {
        super("key");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        String subCommand = options.getString("subCommand", options.getString("subCmd", null));
        switch (subCommand.trim().toLowerCase()) {
        case "refcount":
            return redis.objectRefcount(params[0].asValue().getRaw());
        case "encoding":
            return redis.objectEncoding(params[0].asValue().getRaw());
        case "idletime":
            return redis.objectIdletime(params[0].asValue().getRaw());
        default:
            throw new IllegalArgumentException("Expect options contain sub command in subCmd or subCommand key, got: " + subCommand);
        }
    }
}
