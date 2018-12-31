package io.gridgo.redis.lettuce.delegate;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.redis.command.RedisHyperLogLogCommands;
import io.lettuce.core.api.async.RedisHLLAsyncCommands;

public interface LettuceHyperLogLogCommandsDelegate extends LettuceCommandsDelegate, RedisHyperLogLogCommands {

    <T extends RedisHLLAsyncCommands<byte[], byte[]>> T getHyperLogLogCommands();

    @Override
    default Promise<BElement, Exception> pfadd(byte[] key, byte[]... values) {
        return toPromise(getHyperLogLogCommands().pfadd(key, values));
    }

    @Override
    default Promise<BElement, Exception> pfcount(byte[]... keys) {
        return toPromise(getHyperLogLogCommands().pfcount(keys));
    }

    @Override
    default Promise<BElement, Exception> pfmerge(byte[] destkey, byte[]... sourcekeys) {
        return toPromise(getHyperLogLogCommands().pfmerge(destkey, sourcekeys));
    }
}
