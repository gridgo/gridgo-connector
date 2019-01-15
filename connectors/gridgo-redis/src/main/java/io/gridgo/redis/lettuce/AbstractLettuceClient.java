package io.gridgo.redis.lettuce;

import java.util.function.Function;

import io.gridgo.bean.BElement;
import io.gridgo.framework.impl.AbstractComponentLifecycle;
import io.gridgo.redis.RedisConfig;
import io.gridgo.redis.RedisType;
import io.gridgo.redis.lettuce.delegate.LettuceCommandsDelegate;
import io.gridgo.redis.lettuce.delegate.LettuceGeoCommandsDelegate;
import io.gridgo.redis.lettuce.delegate.LettuceHashCommandsDelegate;
import io.gridgo.redis.lettuce.delegate.LettuceHyperLogLogCommandsDelegate;
import io.gridgo.redis.lettuce.delegate.LettuceKeysCommandsDelegate;
import io.gridgo.redis.lettuce.delegate.LettuceListCommandsDelegate;
import io.gridgo.redis.lettuce.delegate.LettuceScriptingCommandsDelegate;
import io.gridgo.redis.lettuce.delegate.LettuceSetCommandsDelegate;
import io.gridgo.redis.lettuce.delegate.LettuceSortedSetCommandsDelegate;
import io.gridgo.redis.lettuce.delegate.LettuceStringCommandsDelegate;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractLettuceClient extends AbstractComponentLifecycle implements io.gridgo.redis.RedisClient //
        , LettuceCommandsDelegate //
        , LettuceGeoCommandsDelegate //
        , LettuceHashCommandsDelegate //
        , LettuceHyperLogLogCommandsDelegate //
        , LettuceKeysCommandsDelegate //
        , LettuceListCommandsDelegate //
        , LettuceScriptingCommandsDelegate //
        , LettuceSetCommandsDelegate //
        , LettuceSortedSetCommandsDelegate //
        , LettuceStringCommandsDelegate //
{

    @Getter(AccessLevel.PROTECTED)
    private final RedisType type;

    @Getter(AccessLevel.PROTECTED)
    private final RedisConfig config;

    @Getter(AccessLevel.PROTECTED)
    private RedisCodec<byte[], byte[]> codec = new ByteArrayCodec();

    @Getter
    private Function<Object, BElement> parser = this::parse;

    protected AbstractLettuceClient(@NonNull RedisType type, @NonNull RedisConfig config) {
        this.type = type;
        this.config = config;
        if (config.getParser() != null) {
            this.parser = config.getParser();
        }
    }

    @Override
    protected String generateName() {
        return type + "." + config.getAddress();
    }

    private <T> BElement parse(T data) {
        return BElement.ofAny(data);
    }

}
