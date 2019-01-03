package io.gridgo.redis.lettuce.delegate;

import java.util.stream.Collectors;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.redis.command.RedisTransactionCommands;
import io.lettuce.core.api.async.RedisTransactionalAsyncCommands;

public interface LettuceTransactionCommandsDelegate extends LettuceCommandsDelegate, RedisTransactionCommands {

    @Override
    default Promise<BElement, Exception> discard() {
        return toPromise(getTransactionCommands().discard());
    }

    @Override
    default Promise<BElement, Exception> exec() {
        return toPromise(getTransactionCommands().exec()//
                                                 .thenApply(list -> list.stream().map(BElement::ofAny).collect(Collectors.toList())));
    }

    <T extends RedisTransactionalAsyncCommands<byte[], byte[]>> T getTransactionCommands();

    @Override
    default Promise<BElement, Exception> multi() {
        return toPromise(getTransactionCommands().multi());
    }

    @Override
    default Promise<BElement, Exception> unwatch() {
        return toPromise(getTransactionCommands().unwatch());
    }

    @Override
    default Promise<BElement, Exception> watch(byte[]... keys) {
        return toPromise(getTransactionCommands().watch(keys));
    }
}
