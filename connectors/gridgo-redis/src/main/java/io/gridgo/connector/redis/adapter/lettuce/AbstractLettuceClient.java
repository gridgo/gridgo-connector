package io.gridgo.connector.redis.adapter.lettuce;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisConfig;
import io.gridgo.framework.AbstractComponentLifecycle;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractLettuceClient extends AbstractComponentLifecycle implements io.gridgo.connector.redis.adapter.RedisClient {

    @Getter(AccessLevel.PROTECTED)
    private final RedisConfig config;

    @Getter(AccessLevel.PROTECTED)
    private RedisCodec<byte[], byte[]> codec = new ByteArrayCodec();

    private Function<Object, BElement> parser = this::parse;

    protected AbstractLettuceClient(@NonNull RedisConfig config) {
        this.config = config;
        if (config.getParser() != null) {
            this.parser = config.getParser();
        }
    }

    private <T> BElement parse(T data) {
        BElement result;
        if (data instanceof byte[]) {
            try {
                result = BElement.fromRaw((byte[]) data);
            } catch (Exception e) {
                result = BElement.fromAny(data);
            }
        } else {
            result = BElement.fromAny(data);
        }
        return result;
    }

    protected Promise<BElement, Exception> toPromise(RedisFuture<?> future) {
        return new CompletableDeferredObject<BElement, Exception>( //
                (CompletableFuture<BElement>) future.thenApply(this.parser) //
        ).promise();
    }

    @Override
    protected String generateName() {
        return null;
    }

}
