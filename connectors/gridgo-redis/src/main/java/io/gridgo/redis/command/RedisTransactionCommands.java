package io.gridgo.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisTransactionCommands {

    public Promise<BElement, Exception> discard();

    public Promise<BElement, Exception> exec();

    public Promise<BElement, Exception> multi();

    public Promise<BElement, Exception> unwatch();

    public Promise<BElement, Exception> watch(byte[]... keys);

}
