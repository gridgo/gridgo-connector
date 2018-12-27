package io.gridgo.connector.redis.command.set;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.SREM)
public class RedisSremHandler extends AbstractRedisCommandHandler {

    public RedisSremHandler() {
        super("key", "member");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BElement[] params) {
        byte[][] members = new byte[params.length - 1][];
        if (params.length == 2 && params[1].isArray()) {
            BArray array = params[1].asArray();
            int count = 0;
            for (BElement entry : array) {
                members[count++] = entry.asValue().getRaw();
            }
        } else {
            for (int i = 1; i < params.length; i++) {
                members[i - 1] = params[i].asValue().getRaw();
            }
        }
        return redis.srem(params[0].asValue().getRaw(), members);
    }

}
