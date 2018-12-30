package io.gridgo.redis.command.scripting;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SCRIPT_LOAD)
public class RedisScriptLoadHandler extends AbstractRedisCommandHandler {

    public RedisScriptLoadHandler() {
        super("script");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        return redis.scriptLoad(params[0].asValue().getRaw());
    }

}
