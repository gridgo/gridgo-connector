package io.gridgo.connector.redis.command.list;

import java.util.Iterator;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.LPUSH)
public class RedisLpushHandler extends AbstractRedisCommandHandler {

    public RedisLpushHandler() {
        super("key", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        byte[][] values = new byte[params.length - 1][];
        if (params.length == 2 && params[1].isArray()) {
            Iterator<BElement> array = params[1].asArray().iterator();
            int count = 0;
            while (array.hasNext()) {
                values[count++] = array.next().asValue().getRaw();
            }
        } else {
            for (int i = 1; i < params.length; i++) {
                values[i - 1] = params[i].asValue().getRaw();
            }
        }
        return redis.lpush(params[0].asValue().getRaw(), values);
    }
}
