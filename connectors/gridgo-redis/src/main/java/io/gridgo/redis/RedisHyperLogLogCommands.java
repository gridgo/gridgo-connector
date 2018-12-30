package io.gridgo.redis;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisHyperLogLogCommands {

    public Promise<BElement, Exception> pfadd(byte[] key, byte[]... values);

    public Promise<BElement, Exception> pfmerge(byte[] destkey, byte[]... sourcekeys);

    public Promise<BElement, Exception> pfcount(byte[]... keys);
}
