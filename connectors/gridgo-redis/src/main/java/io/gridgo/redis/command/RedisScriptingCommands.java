package io.gridgo.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisScriptingCommands {

    public Promise<BElement, Exception> eval(String script, String outputType, byte[][] keys, byte[]... values);

    public Promise<BElement, Exception> evalsha(String digest, String type, byte[]... keys);

    public Promise<BElement, Exception> evalsha(String digest, String outputType, byte[][] keys, byte[]... values);

    public Promise<BElement, Exception> scriptExists(String... digests);

    public Promise<BElement, Exception> scriptFlush();

    public Promise<BElement, Exception> scriptKill();

    public Promise<BElement, Exception> scriptLoad(byte[] script);
}
