package io.gridgo.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZADD)
public class RedisZaddHandler extends AbstractRedisCommandHandler {

    public RedisZaddHandler() {
        super("key", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        Object[] scoredAndValues = new Object[params.length - 1];
        if (params.length == 2 && params[1].isArray()) {
            BArray array = params[1].asArray();
            for (int i = 0; i < array.size() - 1; i += 2) {
                scoredAndValues[i] = array.get(i).asValue().getDouble();
                scoredAndValues[i + 1] = array.get(i + 1).asValue().getRaw();
            }
        } else {
            for (int i = 1; i < params.length - 1; i += 2) {
                scoredAndValues[i - 1] = params[i].asValue().getDouble();
                scoredAndValues[i] = params[i + 1].asValue().getRaw();
            }
        }

        boolean xx = options.getBoolean("xx", options.getBoolean("XX", false));
        boolean nx = options.getBoolean("nx", options.getBoolean("NX", false));
        boolean ch = options.getBoolean("ch", options.getBoolean("CH", false));

        return redis.zadd(params[0].asValue().getRaw(), xx, nx, ch, scoredAndValues);
    }

}
