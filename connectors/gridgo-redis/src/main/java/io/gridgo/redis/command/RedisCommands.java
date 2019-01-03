package io.gridgo.redis.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisCommands {

    /*
     * STRING CMDS
     */
    public static final String APPEND = "append";
    public static final String BITCOUNT = "bitcount";
    public static final String BITFIELD = "bitfield";
    public static final String BITOP = "bitop";
    public static final String BITPOS = "bitpos";
    public static final String DECR = "decr";
    public static final String DECRBY = "decrby";
    public static final String GET = "get";
    public static final String GETBIT = "getbit";
    public static final String GETRANGE = "getrange";
    public static final String GETSET = "getset";
    public static final String INCR = "incr";
    public static final String INCRBY = "incrby";
    public static final String INCRBYFLOAT = "incrbyfloat";
    public static final String MGET = "mget";
    public static final String MSET = "mset";
    public static final String MSETNX = "msetnx";
    public static final String PSETEX = "psetex";
    public static final String SET = "set";
    public static final String SETBIT = "setbit";
    public static final String SETEX = "setex";
    public static final String SETNX = "setnx";
    public static final String SETRANGE = "setrange";
    public static final String STRLEN = "strlen";

    /*
     * HASH CMDS
     */
    public static final String HDEL = "hdel";
    public static final String HEXISTS = "hexists";
    public static final String HGET = "hget";
    public static final String HGETALL = "hgetall";
    public static final String HINCRBY = "hincrby";
    public static final String HINCRBYFLOAT = "hincrbyfloat";
    public static final String HKEYS = "hkeys";
    public static final String HLEN = "hlen";
    public static final String HMGET = "hmget";
    public static final String HMSET = "hmset";
    public static final String HSET = "hset";
    public static final String HSETNX = "hsetnx";
    public static final String HSTRLEN = "hstrlen";
    public static final String HVALS = "hvals";
    public static final String HSCAN = "hscan";

    /*
     * LIST CMDS
     */
    public static final String BLPOP = "blpop";
    public static final String BRPOP = "brpop";
    public static final String BRPOPLPUSH = "brpoplpush";
    public static final String LINDEX = "lindex";
    public static final String LINSERT = "linsert";
    public static final String LLEN = "llen";
    public static final String LPOP = "lpop";
    public static final String LPUSH = "lpush";
    public static final String LPUSHX = "lpushx";
    public static final String LRANGE = "lrange";
    public static final String LREM = "lrem";
    public static final String LSET = "lset";
    public static final String LTRIM = "ltrim";
    public static final String RPOP = "rpop";
    public static final String RPOPLPUSH = "rpoplpush";
    public static final String RPUSH = "rpush";
    public static final String RPUSHX = "rpushx";

    /*
     * SET CMDS
     */
    public static final String SADD = "sadd";
    public static final String SCARD = "scard";
    public static final String SDIFF = "sdiff";
    public static final String SDIFFSTORE = "sdiffstore";
    public static final String SINTER = "sinter";
    public static final String SINTERSTORE = "sinterstore";
    public static final String SISMEMBER = "sismember";
    public static final String SMEMBERS = "smembers";
    public static final String SMOVE = "smove";
    public static final String SPOP = "spop";
    public static final String SRANDMEMBER = "srandmember";
    public static final String SREM = "srem";
    public static final String SUNION = "sunion";
    public static final String SUNIONSTORE = "sunionstore";
    public static final String SSCAN = "sscan";

    /*
     * SORTED SET CMDS
     */
    public static final String BZPOPMIN = "bzpopmin";
    public static final String BZPOPMAX = "bzpopmax";
    public static final String ZADD = "zadd";
    public static final String ZCARD = "zcard";
    public static final String ZCOUNT = "zcount";
    public static final String ZINCRBY = "zincrby";
    public static final String ZINTERSTORE = "zinterstore";
    public static final String ZLEXCOUNT = "zlexcount";
    public static final String ZPOPMAX = "zpopmax";
    public static final String ZPOPMIN = "zpopmin";
    public static final String ZRANGE = "zrange";
    public static final String ZRANGEBYLEX = "zrangebylex";
    public static final String ZREVRANGEBYLEX = "zrevrangebylex";
    public static final String ZRANGEBYSCORE = "zrangebyscore";
    public static final String ZRANK = "zrank";
    public static final String ZREM = "zrem";
    public static final String ZREMRANGEBYLEX = "zremrangebylex";
    public static final String ZREMRANGEBYRANK = "zremrangebyrank";
    public static final String ZREMRANGEBYSCORE = "zremrangebyscore";
    public static final String ZREVRANGE = "zrevrange";
    public static final String ZREVRANGEBYSCORE = "zrevrangebyscore";
    public static final String ZREVRANK = "zrevrank";
    public static final String ZSCORE = "zscore";
    public static final String ZUNIONSTORE = "zunionstore";
    public static final String ZSCAN = "zscan";

    /*
     * SCRIPTING CMDS
     */
    public static final String EVAL = "eval";
    public static final String EVALSHA = "evalsha";
    public static final String SCRIPT_EXISTS = "scriptexists";
    public static final String SCRIPT_FLUSH = "scriptflush";
    public static final String SCRIPT_KILL = "scriptkill";
    public static final String SCRIPT_LOAD = "scriptload";

    /*
     * GEO CMDS
     */
    public static final String GEOADD = "geoadd";
    public static final String GEOHASH = "geohash";
    public static final String GEOPOS = "geopos";
    public static final String GEODIST = "geodist";
    public static final String GEORADIUS = "georadius";
    public static final String GEORADIUSBYMEMBER = "georadiusbymember";

    /*
     * HYPERLOGLOG CMDS
     */
    public static final String PFADD = "pfadd";
    public static final String PFCOUNT = "pfcount";
    public static final String PFMERGE = "pfmerge";

    /*
     * KEY CMDS
     */
    public static final String DEL = "del";
    public static final String DUMP = "dump";
    public static final String EXISTS = "exists";
    public static final String EXPIRE = "expire";
    public static final String EXPIREAT = "expireat";
    public static final String KEYS = "keys";
    public static final String MIGRATE = "migrate";
    public static final String MOVE = "move";
    public static final String OBJECT = "object";
    public static final String PERSIST = "persist";
    public static final String PEXPIRE = "pexpire";
    public static final String PEXPIREAT = "pexpireat";
    public static final String PTTL = "pttl";
    public static final String RANDOMKEY = "randomkey";
    public static final String RENAME = "rename";
    public static final String RENAMENX = "renamenx";
    public static final String RESTORE = "restore";
    public static final String SORT = "sort";
    public static final String TOUCH = "touch";
    public static final String TTL = "ttl";
    public static final String TYPE = "type";
    public static final String UNLINK = "unlink";
    public static final String SCAN = "scan";

    /*
     * CONNECTION
     */
    public static final String AUTH = "auth";
    public static final String ECHO = "echo";
    public static final String PING = "ping";
    public static final String QUIT = "quit";
    public static final String SELECT = "select";
    public static final String SWAPDB = "swapdb";

    /*
     * TRANSACTION CMDS
     */
    public static final String DISCARD = "discard";
    public static final String EXEC = "exec";
    public static final String MULTI = "multi";
    public static final String UNWATCH = "unwatch";
    public static final String WATCH = "watch";

    private static final Map<String, RedisCommandHandler> handlers = new HashMap<>();

    static {
        scanPackage(RedisCommands.class.getPackageName());
    }

    public static RedisCommandHandler getHandler(String command) {
        if (command == null) {
            return null;
        }
        return handlers.get(command);
    }

    public static void scanPackage(String packageName) {
        scanPackage(packageName, RedisCommands.class.getClassLoader());
    }

    public static void scanPackage(String packageName, ClassLoader classLoader) {
        Reflections reflections = new Reflections(packageName, classLoader);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RedisCommand.class);
        synchronized (handlers) {
            for (Class<?> clazz : classes) {
                if (RedisCommandHandler.class.isAssignableFrom(clazz)) {
                    var cmd = clazz.getAnnotation(RedisCommand.class).value().toLowerCase().trim();
                    if (!handlers.containsKey(cmd)) {
                        try {
                            RedisCommandHandler handler = (RedisCommandHandler) clazz.getConstructor().newInstance();
                            handlers.put(cmd, handler);
                        } catch (Exception e) {
                            throw new RuntimeException("Error while trying to create redis command handler", e);
                        }
                    } else {
                        log.warn("Command '" + cmd + "' (in package '" + packageName + "') has already registered with another handler: "
                                + handlers.get(cmd).getClass().getName());
                    }
                } else {
                    log.warn("class " + clazz + " is annotated with " + RedisCommand.class.getName() + " but doesn't implement "
                            + RedisCommandHandler.class.getName() + " -> ignored");
                }
            }
        }
    }
}
