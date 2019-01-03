package io.gridgo.redis.command.key;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.MIGRATE)
public class RedisMigrateHandler extends AbstractRedisCommandHandler {

    public RedisMigrateHandler() {
        super("host", "port", "db", "timeout");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        boolean copy = options.getBoolean("copy", false);
        boolean replace = options.getBoolean("replace", false);
        byte[] key = options.getRaw("key", null);
        String password = options.getString("password", options.getString("pass", null));

        return redis.migrate(params[0].asValue().getString(), params[1].asValue().getInteger(), params[2].asValue().getInteger(), params[3].asValue().getLong(),
                copy, replace, key, password);
    }

}
