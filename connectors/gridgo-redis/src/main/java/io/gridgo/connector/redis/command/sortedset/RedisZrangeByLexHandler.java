package io.gridgo.connector.redis.command.sortedset;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZREVRANGEBYLEX)
public class RedisZrangeByLexHandler extends AbstractRedisCommandHandler {

    public RedisZrangeByLexHandler() {
        super("key", "lower", "upper");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
        boolean includeLower = options.getBoolean("includeLower", false);
        boolean includeUpper = options.getBoolean("includeUpper", false);
        Long offset = options.getLong("offset", null);
        Long count = options.getLong("count", null);
        return redis.zrangebylex(params[0].asValue().getRaw(), includeLower, params[1].asValue().getRaw(), params[2].asValue().getRaw(), includeUpper, offset,
                count);
    }
}
