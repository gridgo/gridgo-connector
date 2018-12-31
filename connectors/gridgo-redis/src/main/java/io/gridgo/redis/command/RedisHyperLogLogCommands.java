package io.gridgo.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisHyperLogLogCommands {

    public Promise<BElement, Exception> pfadd(byte[] key, byte[]... values);

    public Promise<BElement, Exception> pfcount(byte[]... keys);

    public Promise<BElement, Exception> pfmerge(byte[] destkey, byte[]... sourcekeys);
}
