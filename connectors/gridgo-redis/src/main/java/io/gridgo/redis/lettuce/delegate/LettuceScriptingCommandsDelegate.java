package io.gridgo.redis.lettuce.delegate;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.redis.command.RedisScriptingCommands;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisScriptingAsyncCommands;

public interface LettuceScriptingCommandsDelegate extends LettuceCommandsDelegate, RedisScriptingCommands {

    @Override
    default Promise<BElement, Exception> eval(String script, String type, byte[][] keys, byte[]... values) {
        return toPromise(getScriptingCommands().eval(script, ScriptOutputType.valueOf(type.trim().toUpperCase()), keys, values));
    }

    @Override
    default Promise<BElement, Exception> evalsha(String digest, String outputType, byte[]... keys) {
        ScriptOutputType type = ScriptOutputType.valueOf(outputType.trim().toUpperCase());
        return toPromise(getScriptingCommands().evalsha(digest, type, keys));
    }

    @Override
    default Promise<BElement, Exception> evalsha(String digest, String outputType, byte[][] keys, byte[]... values) {
        ScriptOutputType type = ScriptOutputType.valueOf(outputType.trim().toUpperCase());
        return toPromise(getScriptingCommands().evalsha(digest, type, keys, values));
    }

    <T extends RedisScriptingAsyncCommands<byte[], byte[]>> T getScriptingCommands();

    @Override
    default Promise<BElement, Exception> scriptExists(String... digests) {
        return toPromise(getScriptingCommands().scriptExists(digests));
    }

    @Override
    default Promise<BElement, Exception> scriptFlush() {
        return toPromise(getScriptingCommands().scriptFlush());
    }

    @Override
    default Promise<BElement, Exception> scriptKill() {
        return toPromise(getScriptingCommands().scriptKill());
    }

    @Override
    default Promise<BElement, Exception> scriptLoad(byte[] script) {
        return toPromise(getScriptingCommands().scriptLoad(script));
    }

}
