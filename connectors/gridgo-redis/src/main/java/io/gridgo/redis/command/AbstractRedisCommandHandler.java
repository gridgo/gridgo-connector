package io.gridgo.redis.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BContainer;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.redis.RedisClient;
import io.gridgo.utils.helper.Loggable;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractRedisCommandHandler implements RedisCommandHandler, Loggable {

    protected static final byte[][] EMPTY_BYTES_ARRAY = new byte[0][];
    private static final BElement[] EMPTY_BELEMENT_ARRAY = new BElement[0];

    @Getter
    private final String[] keyOrder;

    protected AbstractRedisCommandHandler(String... keyOrder) {
        this.keyOrder = keyOrder == null ? new String[0] : keyOrder;
    }

    @Override
    public final Promise<BElement, Exception> execute(@NonNull RedisClient redisClient, BObject options, BElement params) {
        BElement[] objs = EMPTY_BELEMENT_ARRAY;
        if (params != null) {
            if (params instanceof BContainer) {
                if (params.isArray()) {
                    objs = params.asArray().toArray(EMPTY_BELEMENT_ARRAY);
                } else if (params.isObject()) {
                    objs = new BElement[this.keyOrder.length];
                    int index = 0;
                    BObject paramsObj = params.asObject();
                    for (String key : keyOrder) {
                        objs[index++] = paramsObj.getOrDefault(key, BValue::ofEmpty);
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

    protected Map<byte[], byte[]> extractMap(BElement[] params, int start) {
        Map<byte[], byte[]> map = new HashMap<>();
        if (params.length == 1 + start && params[start].isObject()) {
            var obj = params[start].asObject();
            for (Entry<String, BElement> entry : obj.entrySet()) {
                map.put(entry.getKey().getBytes(), entry.getValue().asValue().getRaw());
            }
        } else {
            for (int i = start; i < params.length - 1; i += 2) {
                map.put(params[i].asValue().getRaw(), params[i + 1].asValue().getRaw());
            }
        }
        return map;
    }

    protected Map<byte[], byte[]> extractMapFromFirst(BElement[] params) {
        return extractMap(params, 0);
    }

    protected Map<byte[], byte[]> extractMapFromSecond(BElement[] params) {
        return extractMap(params, 1);
    }

    protected abstract Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params);

}
