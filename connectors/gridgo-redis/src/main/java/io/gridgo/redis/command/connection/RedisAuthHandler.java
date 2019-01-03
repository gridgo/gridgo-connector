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

@RedisCommand(RedisCommands.AUTH)
public class RedisAuthHandler extends AbstractRedisCommandHandler {

    public RedisAuthHandler() {
        super("password");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        String reply = redis.auth(params[0].asValue().getString());
        return new SyncDeferredObject<BElement, Exception>().resolve(BValue.of(reply)).promise();
    }
}
