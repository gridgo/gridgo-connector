package io.gridgo.connector.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BContainer;
import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.exception.IllegalRedisCommandsParamsException;
import io.gridgo.utils.helper.Loggable;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractRedisCommandHandler implements RedisCommandHandler, Loggable {

    private static final BElement[] EMPTY_PARAMS = new BElement[0];

    @Getter
    private final String[] keyOrder;

    protected AbstractRedisCommandHandler(String... keyOrder) {
        this.keyOrder = keyOrder == null ? new String[0] : keyOrder;
    }

    @Override
    public final Promise<BElement, Exception> execute(@NonNull RedisClient redisClient, BElement params) {
        BElement[] objs = EMPTY_PARAMS;
        if (params != null) {
            if (params instanceof BContainer) {
                if (((BContainer) params).size() < this.keyOrder.length) {
                    throw new IllegalRedisCommandsParamsException("Expected " + this.keyOrder.length + " params, got " + ((BContainer) params).size());
                }
                if (params.isArray()) {
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
            } else {
                if (this.keyOrder.length > 1) {
                    throw new IllegalRedisCommandsParamsException("Expected " + this.keyOrder.length + " params, got 1 (as a BValue)");
                }
                objs = new BElement[] { params };
            }
        }
        return process(redisClient, objs);
    }

    protected abstract Promise<BElement, Exception> process(RedisClient redis, BElement[] params);
}
