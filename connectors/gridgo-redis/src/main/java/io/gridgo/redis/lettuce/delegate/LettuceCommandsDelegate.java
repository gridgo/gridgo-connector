package io.gridgo.redis.lettuce.delegate;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BElement;
import io.lettuce.core.RedisFuture;

public interface LettuceCommandsDelegate {

    Function<Object, BElement> getParser();

    default Promise<BElement, Exception> toPromise(RedisFuture<?> future) {
        return new CompletableDeferredObject<BElement, Exception>( //
                (CompletableFuture<BElement>) future.thenApply(this.getParser()) //
        ).promise();
    }
}
