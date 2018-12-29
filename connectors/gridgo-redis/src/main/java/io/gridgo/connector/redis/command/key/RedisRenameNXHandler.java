package io.gridgo.connector.redis.command.key;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.RENAMENX)
public class RedisRenameNXHandler extends AbstractRedisCommandHandler {

    public RedisRenameNXHandler() {
        super("key", "newKey");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.renamenx(params[0].asValue().getRaw(), params[1].asValue().getRaw());
    }

}
