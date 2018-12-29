package io.gridgo.connector.redis.command.script;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SCRIPT_EXISTS)
public class RedisScriptExistsHandler extends AbstractRedisCommandHandler {

    public RedisScriptExistsHandler() {
        super("digests");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        String[] digests = new String[params.length];
        int index = 0;
        for (BElement ele : params) {
            digests[index++] = ele.asValue().getString();
        }
        return redis.scriptExists(digests);
    }

}
