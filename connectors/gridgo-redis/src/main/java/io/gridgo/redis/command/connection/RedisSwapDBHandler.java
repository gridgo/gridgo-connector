package io.gridgo.redis.command.connection;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SWAPDB)
public class RedisSwapDBHandler extends AbstractRedisCommandHandler {

    public RedisSwapDBHandler() {
        super("db1", "db2");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.swapdb(params[0].asValue().getInteger(), params[1].asValue().getInteger());
    }
}
