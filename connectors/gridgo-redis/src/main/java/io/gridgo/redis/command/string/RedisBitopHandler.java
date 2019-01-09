package io.gridgo.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;
import io.gridgo.redis.exception.IllegalRedisCommandsParamsException;
import lombok.NonNull;

@RedisCommand(RedisCommands.BITOP)
public class RedisBitopHandler extends AbstractRedisCommandHandler {

    public RedisBitopHandler() {
        super("op", "dest", "src");
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

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        final byte[][] keys = this.parseKeys(params[2]);
        switch (params[0].asValue().getString().toLowerCase()) {
        case "and":
            return redis.bitopAnd(params[1].asValue().getRaw(), keys);
        case "or":
            return redis.bitopOr(params[1].asValue().getRaw(), keys);
        case "xor":
            return redis.bitopXor(params[1].asValue().getRaw(), keys);
        case "not":
            return redis.bitopNot(params[1].asValue().getRaw(), keys[0]);
        default:
        }
        return null;
    }
}
