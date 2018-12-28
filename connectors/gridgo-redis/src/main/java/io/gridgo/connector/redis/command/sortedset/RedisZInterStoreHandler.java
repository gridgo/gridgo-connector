package io.gridgo.connector.redis.command.sortedset;

import java.util.ArrayList;
import java.util.List;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.AbstractRedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommand;
import io.gridgo.connector.redis.command.RedisCommands;

@RedisCommand(RedisCommands.ZINTERSTORE)
public class RedisZInterStoreHandler extends AbstractRedisCommandHandler {

    public RedisZInterStoreHandler() {
        super("destination", "keys");
    }

    @Override
    protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {

        final String aggregate = options.getString("aggregate", null);
        final List<Double> weights = aggregate == null ? null : new ArrayList<>();
        if (aggregate != null) {
            BArray weightsOption = options.getArray("weights", null);
            if (weightsOption != null) {
                weightsOption.forEach(entry -> weights.add(entry.asValue().getDouble()));
            }
        }

        return redis.zinterstore(params[0].asValue().getRaw(), aggregate, weights, extractListBytesFromSecond(params));
    }
}
