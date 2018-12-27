package io.gridgo.connector.redis.command.string;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.MSET)
public class RedisMSetHandler extends AbstractRedisCommandHandler {

    public RedisMSetHandler() {
        super("key", "value");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BElement[] params) {
        Map<byte[], byte[]> map = new HashMap<>();
        if (params.length == 2 && params[1].isObject()) {
            var obj = params[1].asObject();
            for (Entry<String, BElement> entry : obj.entrySet()) {
                map.put(entry.getKey().getBytes(), entry.getValue().asValue().getRaw());
            }
        } else {
            for (int i = 0; i < params.length - 1; i += 2) {
                map.put(params[i].asValue().getRaw(), params[i + 1].asValue().getRaw());
            }
        }
        return redis.mset(map);
    }
}
