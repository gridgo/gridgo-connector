package io.gridgo.redis.lettuce.delegate;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.redis.command.RedisTransactionCommands;

public interface LettuceTransactionCommandsUnsupported extends RedisTransactionCommands {

    @Override
    default Promise<BElement, Exception> discard() {
        throw new UnsupportedOperationException("Method is unsupported in redis cluster");
    }

    @Override
    default Promise<BElement, Exception> exec() {
        throw new UnsupportedOperationException("Method is unsupported in redis cluster");
    }

    @Override
    default Promise<BElement, Exception> multi() {
        throw new UnsupportedOperationException("Method is unsupported in redis cluster");
    }

    @Override
    default Promise<BElement, Exception> unwatch() {
        throw new UnsupportedOperationException("Method is unsupported in redis cluster");
    }

    @Override
    default Promise<BElement, Exception> watch(byte[]... keys) {
        throw new UnsupportedOperationException("Method is unsupported in redis cluster");
    }
}
