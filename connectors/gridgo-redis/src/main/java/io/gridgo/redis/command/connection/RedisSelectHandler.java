package io.gridgo.redis.command.connection;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.SyncDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SELECT)
public class RedisSelectHandler extends AbstractRedisCommandHandler {

    public RedisSelectHandler() {
        super("db");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        String reply = redis.select(params[0].asValue().getInteger());
        return new SyncDeferredObject<BElement, Exception>().resolve(BValue.of(reply)).promise();
    }
}
