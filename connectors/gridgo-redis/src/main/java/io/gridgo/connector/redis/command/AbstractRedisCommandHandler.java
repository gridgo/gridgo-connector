package io.gridgo.connector.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.utils.helper.Loggable;
import lombok.NonNull;

public abstract class AbstractRedisCommandHandler implements RedisCommandHandler, Loggable {

    private final String[] keyOrder;

    protected AbstractRedisCommandHandler(String... keyOrder) {
        this.keyOrder = keyOrder == null ? new String[0] : keyOrder;
    }

    @Override
    public final Promise<BElement, Exception> execute(@NonNull RedisClient redisClient, BElement params) {
        Object[] objs = null;
        if (params != null) {
            if (params.isArray()) {
                objs = params.asArray().toArray();
            } else if (params.isObject()) {
                objs = new Object[params.asObject().size()];
                int count = 0;
                for (String key : keyOrder) {
                    if (count >= objs.length) {
                        break;
                    }
                    objs[count++] = 
                }
            }
        }
        return process(redisClient, objs);
    }

    protected abstract Promise<BElement, Exception> process(RedisClient redis, Object... params);
}
