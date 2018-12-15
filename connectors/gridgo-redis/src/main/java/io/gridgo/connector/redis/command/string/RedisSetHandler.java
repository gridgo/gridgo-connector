package io.gridgo.connector.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommands;
import io.gridgo.connector.redis.exception.IllegalRedisCommandsParamsException;

@RedisCommand(RedisCommands.SET)
public class RedisSetHandler implements RedisCommandHandler {

    @Override
    public Promise<BElement, Exception> execute(RedisClient redisClient, BElement params) {
        String key = null;
        String value = null;
        if (params.isArray()) {
            key = params.asArray().getString(0);
            value = params.asArray().getString(1);
        } else if (params.isObject()) {
            key = params.asObject().getString("key");
            value = params.asObject().getString("value");
        }
        if (key == null) {
            throw new IllegalRedisCommandsParamsException(
                    "Illegal params for command SET, expected for BArray (with atleast 1 elements for key - 1st - and value - 2nd) or BObject (key and value). Got: "
                            + params);
        }
        return redisClient.set(key.getBytes(), value == null ? null : value.getBytes());
    }

}
