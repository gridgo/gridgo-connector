package io.gridgo.connector.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BContainer;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.redis.adapter.RedisClient;
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
    public final Promise<BElement, Exception> execute(@NonNull RedisClient redisClient, BObject options, BElement params) {
        BElement[] objs = EMPTY_PARAMS;
        if (params != null) {
            if (params instanceof BContainer) {
                if (params.isArray()) {
                    objs = params.asArray().toArray(EMPTY_PARAMS);
                } else if (params.isObject()) {
                    objs = new BElement[this.keyOrder.length];
                    int count = 0;
                    BObject paramsObjs = params.asObject();
                    for (String key : keyOrder) {
                        if (paramsObjs.containsKey(key)) {
                            objs[count++] = paramsObjs.get(key);
                        } else {
                            objs[count++] = BValue.ofEmpty();
                        }
                    }
                }
            } else {
                objs = new BElement[] { params };
            }
        }
        return process(redisClient, options == null ? BObject.ofEmpty() : options, objs);
    }

    protected byte[][] extractListBytes(BElement[] params, int start) {
        byte[][] members = new byte[params.length - start][];
        if (params.length == 1 + start && params[start].isArray()) {
            BArray array = params[start].asArray();
            int count = 0;
            for (BElement entry : array) {
                members[count++] = entry.asValue().getRaw();
            }
        } else {
            for (int i = start; i < params.length; i++) {
                members[i - start] = params[i].asValue().getRaw();
            }
        }
        return members;
    }

    protected byte[][] extractListBytesFromFirst(BElement[] params) {
        return extractListBytes(params, 0);
    }

    protected byte[][] extractListBytesFromSecond(BElement[] params) {
        return extractListBytes(params, 1);
    }

    protected abstract Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params);
}
