package io.gridgo.connector.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;
import io.gridgo.connector.redis.exception.IllegalRedisCommandsParamsException;
import lombok.NonNull;

@RedisCommand(RedisCommands.BITFIELD)
public class RedisBitopHandler extends AbstractRedisCommandHandler {

    public RedisBitopHandler() {
        super("op", "dest", "src");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BElement[] params) {
        final byte[][] keys = this.parseKeys(params[2]);
        switch (params[0].asValue().getString().toLowerCase()) {
        case "and":
            return redis.bitopAnd(params[1].asValue().getRaw(), keys);
        case "or":
            return redis.bitopOr(params[1].asValue().getRaw(), keys);
        case "xor":
            return redis.bitopXor(params[1].asValue().getRaw(), keys);
        case "not":
            return redis.bitopAnd(params[1].asValue().getRaw(), keys[0]);
        }
        return null;
    }

    private byte[][] parseKeys(@NonNull BElement keys) {
        byte[][] result = null;
        if (keys.isValue()) {
            result = new byte[][] { keys.asValue().getRaw() };
        } else if (keys.isArray()) {
            BArray asArray = keys.asArray();
            result = new byte[asArray.size()][];
            for (int i = 0; i < asArray.size(); i++) {
                result[i] = asArray.getRaw(i);
            }
        } else {
            throw new IllegalRedisCommandsParamsException("Bitop keys must be BValue or BArray");
        }
        return result;
    }
}
