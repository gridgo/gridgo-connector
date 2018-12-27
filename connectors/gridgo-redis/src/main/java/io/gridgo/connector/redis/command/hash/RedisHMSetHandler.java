package io.gridgo.connector.redis.command.hash;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.HMSET)
public class RedisHMSetHandler extends AbstractRedisCommandHandler {

    public RedisHMSetHandler() {
        super("key", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BElement[] params) {
        Map<byte[], byte[]> values = new HashMap<>();
        if (params.length == 2 && params[1].isObject()) {
            var obj = params[1].asObject();
            for (Entry<String, BElement> entry : obj.entrySet()) {
                values.put(entry.getKey().getBytes(), entry.getValue().asValue().getRaw());
            }
        } else {
            for (int i = 1; i < params.length - 1; i += 2) {
                values.put(params[i].asValue().getRaw(), params[i + 1].asValue().getRaw());
            }
        }
        return redis.hmset(params[0].asValue().getRaw(), values);
    }
}
