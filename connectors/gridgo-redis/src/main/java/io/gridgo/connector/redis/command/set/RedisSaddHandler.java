package io.gridgo.connector.redis.command.set;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SADD)
public class RedisSaddHandler extends AbstractRedisCommandHandler {

    public RedisSaddHandler() {
        super("key", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        byte[][] value = new byte[params.length - 1][];
        if (params.length == 2 && params[1].isArray()) {
            BArray array = params[1].asArray();
            int count = 0;
            for (BElement entry : array) {
                value[count++] = entry.asValue().getRaw();
            }
        } else {
            for (int i = 1; i < params.length; i++) {
                value[i - 1] = params[i].asValue().getRaw();
            }
        }
        return redis.sadd(params[0].asValue().getRaw(), value);
    }

}
