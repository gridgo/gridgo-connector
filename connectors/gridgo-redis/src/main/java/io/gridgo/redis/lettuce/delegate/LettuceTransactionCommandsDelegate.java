package io.gridgo.redis.lettuce.delegate;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.redis.adapter.RedisTransactionCommands;
import io.lettuce.core.api.async.RedisTransactionalAsyncCommands;

public interface LettuceTransactionCommandsDelegate extends LettuceCommandsDelegate, RedisTransactionCommands {

    <T extends RedisTransactionalAsyncCommands<byte[], byte[]>> T getTransactionCommands();

    @Override
    default Promise<BElement, Exception> exec() {
        return toPromise(getTransactionCommands().exec());
    }

    @Override
    default Promise<BElement, Exception> multi() {
        return toPromise(getTransactionCommands().multi());
    }

    @Override
    default Promise<BElement, Exception> watch(byte[]... keys) {
        return toPromise(getTransactionCommands().watch(keys));
    }

    @Override
    default Promise<BElement, Exception> unwatch() {
        return toPromise(getTransactionCommands().unwatch());
    }

    @Override
    default Promise<BElement, Exception> discard() {
        return toPromise(getTransactionCommands().discard());
    }
}
