package io.gridgo.connector.redis.command.list;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.LTRIM)
public class RedisLtrimHandler extends AbstractRedisCommandHandler {

    public RedisLtrimHandler() {
        super("key", "start", "stop");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.ltrim(params[0].asValue().getRaw(), params[1].asValue().getLong(), params[2].asValue().getLong());
    }
}
