package io.gridgo.connector.redis.command.key;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.OBJECT)
public class RedisObjectHandler extends AbstractRedisCommandHandler {

    public RedisObjectHandler() {
        super("key", "db");
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
        }
        throw new IllegalArgumentException("Expect options contain sub command in subCmd or subCommand key, got: " + subCommand);
    }
}
