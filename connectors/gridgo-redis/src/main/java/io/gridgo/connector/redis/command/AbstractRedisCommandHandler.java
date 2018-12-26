package io.gridgo.connector.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BContainer;
import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.exception.IllegalRedisCommandsParamsException;
import io.gridgo.utils.helper.Loggable;
import lombok.NonNull;

public abstract class AbstractRedisCommandHandler implements RedisCommandHandler, Loggable {

    private static final BElement[] EMPTY_PARAMS = new BElement[0];
    private final String[] keyOrder;
    private final int numOptional;

    protected AbstractRedisCommandHandler(int numOptional, String... keyOrder) {
        this.numOptional = numOptional;
        this.keyOrder = keyOrder == null ? new String[0] : keyOrder;
    }

    protected AbstractRedisCommandHandler(String... keyOrder) {
        this(0, keyOrder);
    }

    @Override
    public final Promise<BElement, Exception> execute(@NonNull RedisClient redisClient, BElement params) {
        BElement[] objs = EMPTY_PARAMS;
        if (params != null) {
            if (params instanceof BContainer && ((BContainer) params).size() < this.keyOrder.length) {
                throw new IllegalRedisCommandsParamsException("Expected " + this.keyOrder.length + " params, got " + ((BContainer) params).size());
            } else if (this.keyOrder.length > 1 && params.isValue()) {
                throw new IllegalRedisCommandsParamsException("Expected " + this.keyOrder.length + " params, got 1 (as a BValue)");
            }
            if (params.isValue()) {
                objs = new BElement[] { params.asValue() };
            } else if (params.isArray()) {
                objs = params.asArray().toArray(EMPTY_PARAMS);
            } else if (params.isObject()) {
                objs = new BElement[this.keyOrder.length];
                int count = 0;
                for (String key : keyOrder) {
                    if (!params.asObject().containsKey(key)) {
                        throw new IllegalRedisCommandsParamsException("Params as BObject require for key " + key + " but missing");
                    }
                    objs[count++] = params.asObject().get(key);
                }
            }
        }
        return process(redisClient, objs);
    }

    protected abstract Promise<BElement, Exception> process(RedisClient redis, BElement[] params);
}
