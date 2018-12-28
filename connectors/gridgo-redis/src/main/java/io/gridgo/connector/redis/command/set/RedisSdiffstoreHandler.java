package io.gridgo.connector.redis.command.set;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SDIFFSTORE)
public class RedisSdiffstoreHandler extends AbstractRedisCommandHandler {

    public RedisSdiffstoreHandler() {
        super("destination", "keys");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        byte[][] keys = new byte[params.length - 1][];
        if (params.length == 2 && params[1].isArray()) {
            BArray array = params[1].asArray();
            int count = 0;
            for (BElement entry : array) {
                keys[count++] = entry.asValue().getRaw();
            }
        } else {
            for (int i = 1; i < params.length; i++) {
                keys[i - 1] = params[i].asValue().getRaw();
            }
        }
        return redis.sdiffstore(params[0].asValue().getRaw(), keys);
    }

}
