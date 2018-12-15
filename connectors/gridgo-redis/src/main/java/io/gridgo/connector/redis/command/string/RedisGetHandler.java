package io.gridgo.connector.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommands;
import io.gridgo.connector.redis.exception.IllegalRedisCommandsParamsException;

@RedisCommand(RedisCommands.GET)
public class RedisGetHandler implements RedisCommandHandler {

    @Override
    public Promise<BElement, Exception> execute(RedisClient redisClient, BElement params) {
        String key = null;
        if (params.isArray()) {
            key = params.asArray().getString(0);
        } else if (params.isObject()) {
            key = params.asObject().getString("key");
        } else if (params.isValue()) {
            key = params.asValue().getString();
        }
        if (key == null) {
            throw new IllegalRedisCommandsParamsException(
                    "Illegal params for command GET, expected for BArray (with 1 element) or BObject (key) or BValue. Got: " + params);
        }
        return redisClient.get(key.getBytes());
    }

}
