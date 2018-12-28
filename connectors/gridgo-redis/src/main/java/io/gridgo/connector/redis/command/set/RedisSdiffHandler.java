package io.gridgo.connector.redis.command.set;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SDIFF)
public class RedisSdiffHandler extends AbstractRedisCommandHandler {

    public RedisSdiffHandler() {
        super("keys");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        byte[][] keys = new byte[params.length][];
        if (params.length == 1 && params[0].isArray()) {
            BArray array = params[0].asArray();
            int count = 0;
            for (BElement entry : array) {
                keys[count++] = entry.asValue().getRaw();
            }
        } else {
            for (int i = 0; i < params.length; i++) {
                keys[i] = params[i].asValue().getRaw();
            }
        }
        return redis.sdiff(keys);
    }

}
