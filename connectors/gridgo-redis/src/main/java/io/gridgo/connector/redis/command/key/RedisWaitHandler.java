package io.gridgo.connector.redis.command.key;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.WAIT)
public class RedisWaitHandler extends AbstractRedisCommandHandler {

    public RedisWaitHandler() {
        super("replicas", "timeout");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.waitForReplication(params[0].asValue().getInteger(), params[1].asValue().getLong());
    }

}
