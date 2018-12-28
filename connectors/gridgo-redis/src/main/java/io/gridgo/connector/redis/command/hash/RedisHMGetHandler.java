package io.gridgo.connector.redis.command.hash;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.HMGET)
public class RedisHMGetHandler extends AbstractRedisCommandHandler {

    public RedisHMGetHandler() {
        super("key", "fields");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        byte[][] fields = new byte[params.length - 1][];
        if (params.length == 2 && params[1].isArray()) {
            BArray asArray = params[1].asArray();
            for (int i = 0; i < asArray.size(); i++) {
                fields[i] = asArray.get(i).asValue().getRaw();
            }
        } else {
            for (int i = 1; i < params.length; i++) {
                fields[i - 1] = params[i].asValue().getRaw();
            }
        }
        return redis.hmget(params[0].asValue().getRaw(), fields);
    }
}
