package io.gridgo.redis;

import io.gridgo.framework.ComponentLifecycle;

public interface RedisClient extends ComponentLifecycle, RedisStringCommands, RedisHashCommands, RedisListCommands, RedisSetCommands, RedisSortedSetCommands,
        RedisScriptingCommands, RedisGeoCommands, RedisHyperLogLogCommands, RedisKeysCommands, RedisConnectionCommands, RedisTransactionCommands {

}
