package io.gridgo.redis;

import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.redis.command.RedisConnectionCommands;
import io.gridgo.redis.command.RedisGeoCommands;
import io.gridgo.redis.command.RedisHashCommands;
import io.gridgo.redis.command.RedisHyperLogLogCommands;
import io.gridgo.redis.command.RedisKeysCommands;
import io.gridgo.redis.command.RedisListCommands;
import io.gridgo.redis.command.RedisScriptingCommands;
import io.gridgo.redis.command.RedisSetCommands;
import io.gridgo.redis.command.RedisSortedSetCommands;
import io.gridgo.redis.command.RedisStringCommands;
import io.gridgo.redis.command.RedisTransactionCommands;

public interface RedisClient extends ComponentLifecycle, RedisStringCommands, RedisHashCommands, RedisListCommands, RedisSetCommands, RedisSortedSetCommands,
        RedisScriptingCommands, RedisGeoCommands, RedisHyperLogLogCommands, RedisKeysCommands, RedisConnectionCommands, RedisTransactionCommands {

}
