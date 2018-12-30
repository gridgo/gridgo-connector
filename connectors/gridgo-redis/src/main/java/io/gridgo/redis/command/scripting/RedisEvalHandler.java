package io.gridgo.redis.command.scripting;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;

@RedisCommand(RedisCommands.EVAL)
public class RedisEvalHandler extends AbstractRedisCommandHandler {

    public RedisEvalHandler() {
        super("script", "args");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        String outputType = options.getString("scriptOutputType", "status");
        if (params.length > 1) {
            var numKeys = options.getInteger("numKeys", -1);
            byte[][] keys;
            byte[][] values;
            if (numKeys < 0) {
                values = EMPTY_BYTES_ARRAY;
                keys = extractListBytesFromSecond(params);
            } else {
                keys = new byte[numKeys][];
                values = new byte[params.length - 1 - numKeys][];
                for (int i = 1; i < params.length; i++) {
                    if (i - 1 < numKeys) {
                        keys[i - 1] = params[i].asValue().getRaw();
                    } else {
                        values[i - 1] = params[i].asValue().getRaw();
                    }
                }
            }
            return redis.eval(params[0].asValue().getString(), outputType, keys, values);
        }
        return redis.eval(params[0].asValue().getString(), outputType, EMPTY_BYTES_ARRAY, EMPTY_BYTES_ARRAY);
    }

}
