package io.gridgo.redis.lettuce.delegate;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.lettuce.core.KeyValue;
import lombok.NonNull;

public interface LettuceCommandsDelegate {

    default <T, U> List<U> convertList(@NonNull List<T> list, Function<T, U> mapper) {
        return list.stream().map(mapper).collect(Collectors.toList());
    }

    Function<Object, BElement> getParser();

    default BArray keyValueToBArray(KeyValue<byte[], byte[]> keyValue) {
        return BArray.ofSequence(keyValue.getKey(), keyValue.getValue());
    }

    default Promise<BElement, Exception> toPromise(@NonNull CompletionStage<?> future) {
        return new CompletableDeferredObject<BElement, Exception>(future.toCompletableFuture().thenApply(this.getParser())).promise();
    }
}
