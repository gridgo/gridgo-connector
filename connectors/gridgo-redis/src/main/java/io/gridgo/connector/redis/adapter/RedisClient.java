package io.gridgo.connector.redis.adapter;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.framework.ComponentLifecycle;
import io.lettuce.core.BitFieldArgs;
import io.lettuce.core.Consumer;
import io.lettuce.core.KillArgs;
import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.SetArgs;
import io.lettuce.core.SortArgs;
import io.lettuce.core.UnblockType;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.XClaimArgs;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XReadArgs.StreamOffset;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.ScoredValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingChannel;
import io.lettuce.core.protocol.CommandType;

@SuppressWarnings("unchecked")
public interface RedisClient extends ComponentLifecycle {

    public Promise<BElement, Exception> geoadd(byte[] key, double longitude, double latitude, byte[] member);

    public Promise<BElement, Exception> pfadd(byte[] key, byte[]... values);

    public Promise<BElement, Exception> discard();

    public Promise<BElement, Exception> xack(byte[] key, byte[] group, String... messageIds);

    public Promise<BElement, Exception> sadd(byte[] key, byte[]... members);

    public Promise<BElement, Exception> blpop(long timeout, byte[]... keys);

    public Promise<BElement, Exception> bzpopmin(long timeout, byte[]... keys);

    public Promise<BElement, Exception> bgrewriteaof();

    public Promise<BElement, Exception> publish(byte[] channel, byte[] message);

    public Promise<BElement, Exception> append(byte[] key, byte[] value);

    public Promise<BElement, Exception> hdel(byte[] key, byte[]... fields);

    public Promise<BElement, Exception> exec();

    public Promise<BElement, Exception> unlink(byte[]... keys);

    public Promise<BElement, Exception> pfmerge(byte[] destkey, byte[]... sourcekeys);

    public Promise<BElement, Exception> bgsave();

    public Promise<BElement, Exception> eval(String script, String outputType, byte[][] keys, byte[]... values);

    public Promise<BElement, Exception> geoadd(byte[] key, Object... lngLatMember);

    public Promise<BElement, Exception> xadd(byte[] key, Map<byte[], byte[]> body);

    public Promise<BElement, Exception> scard(byte[] key);

    public Promise<BElement, Exception> bitcount(byte[] key);

    public Promise<BElement, Exception> pubsubChannels();

    public Promise<BElement, Exception> clientGetname();

    public Promise<BElement, Exception> multi();

    public Promise<BElement, Exception> hexists(byte[] key, byte[] field);

    public Promise<BElement, Exception> bzpopmax(long timeout, byte[]... keys);

    public String auth(String password);

    public void setTimeout(Duration timeout);

    public Promise<BElement, Exception> dump(byte[] key);

    public Promise<BElement, Exception> bitcount(byte[] key, long start, long end);

    public Promise<BElement, Exception> xadd(byte[] key, XAddArgs args, Map<byte[], byte[]> body);

    public Promise<BElement, Exception> pfcount(byte[]... keys);

    public Promise<BElement, Exception> pubsubChannels(byte[] channel);

    public Promise<BElement, Exception> watch(byte[]... keys);

    public Promise<BElement, Exception> sdiff(byte[]... keys);

    public Promise<BElement, Exception> clientSetname(byte[] name);

    public Promise<BElement, Exception> brpop(long timeout, byte[]... keys);

    public Promise<BElement, Exception> evalsha(String digest, String type, byte[]... keys);

    public Promise<BElement, Exception> geohash(byte[] key, byte[]... members);

    public String select(int db);

    public Promise<BElement, Exception> exists(byte[]... keys);

    public Promise<BElement, Exception> sdiff(ValueStreamingChannel<byte[]> channel, byte[]... keys);

    public Promise<BElement, Exception> bitfield(byte[] key, BitFieldArgs bitFieldArgs);

    public Promise<BElement, Exception> swapdb(int db1, int db2);

    public Promise<BElement, Exception> unwatch();

    public Promise<BElement, Exception> xadd(byte[] key, Object... keysAndValues);

    public Promise<BElement, Exception> clientKill(String addr);

    public Promise<BElement, Exception> hget(byte[] key, byte[] field);

    public Promise<BElement, Exception> pubsubNumsub(byte[]... channels);

    public Promise<BElement, Exception> zadd(byte[] key, double score, byte[] member);

    public Promise<BElement, Exception> expire(byte[] key, long seconds);

    public Promise<BElement, Exception> evalsha(String digest, String outputType, byte[][] keys, byte[]... values);

    public Promise<BElement, Exception> georadiusbymember(byte[] key, byte[] member, double distance, String unit, byte[] storeKey, byte[] storeDistKey,
            Long count, String sort);

    public Promise<BElement, Exception> georadiusbymember(byte[] key, byte[] member, double distance, String unitStr, boolean withdistance,
            boolean withcoordinates, boolean withHash, Long count, String sort);

    public Promise<BElement, Exception> georadius(byte[] key, double longitude, double latitude, double distance, String unit, boolean withdistance,
            boolean withcoordinates, boolean withHash, Long count, String sort);

    public Promise<BElement, Exception> georadius(byte[] key, double longitude, double latitude, double distance, String unit, byte[] storeKey,
            byte[] storeDistKey, Long count, String sort);

    public Promise<BElement, Exception> sdiffstore(byte[] destination, byte[]... keys);

    public Promise<BElement, Exception> xadd(byte[] key, XAddArgs args, Object... keysAndValues);

    public Promise<BElement, Exception> clusterBumpepoch();

    public Promise<BElement, Exception> clientKill(KillArgs killArgs);

    public Promise<BElement, Exception> bitpos(byte[] key, boolean state);

    public Promise<BElement, Exception> pubsubNumpat();

    public Promise<BElement, Exception> hincrby(byte[] key, byte[] field, long amount);

    public Promise<BElement, Exception> brpoplpush(long timeout, byte[] source, byte[] destination);

    public Promise<BElement, Exception> expireat(byte[] key, Date timestamp);

    public Promise<BElement, Exception> scriptExists(String... digests);

    public Promise<BElement, Exception> sinter(byte[]... keys);

    public Promise<BElement, Exception> clientUnblock(long id, UnblockType type);

    public Promise<BElement, Exception> zadd(byte[] key, Object... scoresAndValues);

    public Promise<BElement, Exception> xclaim(byte[] key, Consumer<byte[]> consumer, long minIdleTime, String... messageIds);

    public Promise<BElement, Exception> echo(byte[] msg);

    public Promise<BElement, Exception> clusterMeet(String ip, int port);

    public Promise<BElement, Exception> role();

    public Promise<BElement, Exception> hincrbyfloat(byte[] key, byte[] field, double amount);

    public Promise<BElement, Exception> sinter(ValueStreamingChannel<byte[]> channel, byte[]... keys);

    public Promise<BElement, Exception> clientPause(long timeout);

    public Promise<BElement, Exception> lindex(byte[] key, long index);

    public Promise<BElement, Exception> expireat(byte[] key, long timestamp);

    public Promise<BElement, Exception> scriptFlush();

    public Promise<BElement, Exception> sinterstore(byte[] destination, byte[]... keys);

    public Promise<BElement, Exception> ping();

    public Promise<BElement, Exception> xclaim(byte[] key, Consumer<byte[]> consumer, XClaimArgs args, String... messageIds);

    public Promise<BElement, Exception> clusterForget(String nodeId);

    public Promise<BElement, Exception> hgetall(byte[] key);

    public Promise<BElement, Exception> clientList();

    public Promise<BElement, Exception> scriptKill();

    public Promise<BElement, Exception> readOnly();

    public Promise<BElement, Exception> linsert(byte[] key, boolean before, byte[] pivot, byte[] value);

    public Promise<BElement, Exception> scriptLoad(byte[] script);

    public Promise<BElement, Exception> sismember(byte[] key, byte[] member);

    public Promise<BElement, Exception> clusterAddSlots(int... slots);

    public Promise<BElement, Exception> readWrite();

    public Promise<BElement, Exception> bitpos(byte[] key, boolean state, long start);

    public Promise<BElement, Exception> keys(byte[] pattern);

    public Promise<BElement, Exception> hgetall(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key);

    public Promise<BElement, Exception> command();

    public Promise<BElement, Exception> quit();

    public Promise<BElement, Exception> xdel(byte[] key, String... messageIds);

    public Promise<BElement, Exception> clusterDelSlots(int... slots);

    public Promise<BElement, Exception> llen(byte[] key);

    public Promise<BElement, Exception> keys(KeyStreamingChannel<byte[]> channel, byte[] pattern);

    public Promise<BElement, Exception> commandInfo(String... cmds);

    public Promise<BElement, Exception> hkeys(byte[] key);

    public Promise<BElement, Exception> smove(byte[] source, byte[] destination, byte[] member);

    public Promise<BElement, Exception> lpop(byte[] key);

    public Promise<BElement, Exception> clusterSetSlotNode(int slot, String nodeId);

    public Promise<BElement, Exception> commandInfo(CommandType... cmds);

    public Promise<BElement, Exception> waitForReplication(int replicas, long timeout);

    public Promise<BElement, Exception> migrate(String host, int port, int db, long timeout, boolean copy, boolean replace, byte[] keys, String password);

    public Promise<BElement, Exception> hkeys(KeyStreamingChannel<byte[]> channel, byte[] key);

    public Promise<BElement, Exception> xgroupCreate(StreamOffset<byte[]> streamOffset, byte[] group);

    public Promise<BElement, Exception> zadd(byte[] key, boolean xx, boolean nx, boolean ch, Object... scoresAndValues);

    public Promise<BElement, Exception> lpush(byte[] key, byte[]... values);

    public Promise<BElement, Exception> commandCount();

    public Promise<BElement, Exception> hlen(byte[] key);

    public Promise<BElement, Exception> clusterSetSlotStable(int slot);

    public Promise<BElement, Exception> smembers(byte[] key);

    public Promise<BElement, Exception> xgroupDelconsumer(byte[] key, Consumer<byte[]> consumer);

    public Promise<BElement, Exception> lpushx(byte[] key, byte[]... values);

    public Promise<BElement, Exception> configGet(String parameter);

    public Promise<BElement, Exception> hmget(byte[] key, byte[]... fields);

    public Promise<BElement, Exception> smembers(ValueStreamingChannel<byte[]> channel, byte[] key);

    public Promise<BElement, Exception> clusterSetSlotMigrating(int slot, String nodeId);

    public Promise<BElement, Exception> bitpos(byte[] key, boolean state, long start, long end);

    public Promise<BElement, Exception> xgroupDestroy(byte[] key, byte[] group);

    public Promise<BElement, Exception> configResetstat();

    public Promise<BElement, Exception> lrange(byte[] key, long start, long stop);

    public Promise<BElement, Exception> spop(byte[] key);

    public Promise<BElement, Exception> hmget(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key, byte[]... fields);

    public Promise<BElement, Exception> configRewrite();

    public Promise<BElement, Exception> xgroupSetid(StreamOffset<byte[]> streamOffset, byte[] group);

    public Promise<BElement, Exception> move(byte[] key, int db);

    public Promise<BElement, Exception> clusterSetSlotImporting(int slot, String nodeId);

    public Promise<BElement, Exception> lrange(ValueStreamingChannel<byte[]> channel, byte[] key, long start, long stop);

    public Promise<BElement, Exception> spop(byte[] key, long count);

    public Promise<BElement, Exception> hmset(byte[] key, Map<byte[], byte[]> map);

    public Promise<BElement, Exception> configSet(String parameter, String value);

    public Promise<BElement, Exception> objectEncoding(byte[] key);

    public boolean isOpen();

    public Promise<BElement, Exception> zaddincr(byte[] key, double score, byte[] member);

    public Promise<BElement, Exception> xlen(byte[] key);

    public void reset();

    public Promise<BElement, Exception> clusterInfo();

    public Promise<BElement, Exception> hscan(byte[] key);

    public Promise<BElement, Exception> srandmember(byte[] key);

    public Promise<BElement, Exception> objectIdletime(byte[] key);

    public Promise<BElement, Exception> lrem(byte[] key, long count, byte[] value);

    public Promise<BElement, Exception> xpending(byte[] key, byte[] group);

    public Promise<BElement, Exception> dbsize();

    public Promise<BElement, Exception> clusterMyId();

    public void setAutoFlushCommands(boolean autoFlush);

    public Promise<BElement, Exception> hscan(byte[] key, ScanArgs scanArgs);

    public Promise<BElement, Exception> debugCrashAndRecover(Long delay);

    public Promise<BElement, Exception> lset(byte[] key, long index, byte[] value);

    public Promise<BElement, Exception> xpending(byte[] key, byte[] group, Range<String> range, Limit limit);

    public Promise<BElement, Exception> clusterNodes();

    public Promise<BElement, Exception> objectRefcount(byte[] key);

    public Promise<BElement, Exception> srandmember(byte[] key, long count);

    public Promise<BElement, Exception> debugHtstats(int db);

    public Promise<BElement, Exception> hscan(byte[] key, ScanCursor scanCursor, ScanArgs scanArgs);

    public Promise<BElement, Exception> persist(byte[] key);

    public Promise<BElement, Exception> ltrim(byte[] key, long start, long stop);

    public Promise<BElement, Exception> clusterSlaves(String nodeId);

    public void flushCommands();

    public Promise<BElement, Exception> debugObject(byte[] key);

    public Promise<BElement, Exception> bitopAnd(byte[] destination, byte[]... keys);

    public Promise<BElement, Exception> geopos(byte[] key, byte[]... members);

    public Promise<BElement, Exception> zcard(byte[] key);

    public Promise<BElement, Exception> srandmember(ValueStreamingChannel<byte[]> channel, byte[] key, long count);

    public Promise<BElement, Exception> xpending(byte[] key, Consumer<byte[]> consumer, Range<String> range, Limit limit);

    public Promise<BElement, Exception> rpop(byte[] key);

    public Promise<BElement, Exception> hscan(byte[] key, ScanCursor scanCursor);

    public void debugOom();

    public Promise<BElement, Exception> pexpire(byte[] key, long milliseconds);

    public void debugSegfault();

    public Promise<BElement, Exception> bitopNot(byte[] destination, byte[] source);

    public Promise<BElement, Exception> clusterGetKeysInSlot(int slot, int count);

    public Promise<BElement, Exception> rpoplpush(byte[] source, byte[] destination);

    /**
     * 
     * @param key
     * @param from
     * @param to
     * @param unit one of m, km, ft, mi
     * @return
     */
    public Promise<BElement, Exception> geodist(byte[] key, byte[] from, byte[] to, String unit);

    public Promise<BElement, Exception> srem(byte[] key, byte[]... members);

    public Promise<BElement, Exception> debugReload();

    public Promise<BElement, Exception> hscan(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key);

    public Promise<BElement, Exception> xrange(byte[] key, Range<String> range);

    public Promise<BElement, Exception> pexpireat(byte[] key, Date timestamp);

    public Promise<BElement, Exception> clusterCountKeysInSlot(int slot);

    public Promise<BElement, Exception> debugRestart(Long delay);

    public Promise<BElement, Exception> rpush(byte[] key, byte[]... values);

    public Promise<BElement, Exception> sunion(byte[]... keys);

    public Promise<BElement, Exception> bitopOr(byte[] destination, byte[]... keys);

    public Promise<BElement, Exception> hscan(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key, ScanArgs scanArgs);

    public Promise<BElement, Exception> debugSdslen(byte[] key);

    public Promise<BElement, Exception> xrange(byte[] key, Range<String> range, Limit limit);

    public Promise<BElement, Exception> sunion(ValueStreamingChannel<byte[]> channel, byte[]... keys);

    public Promise<BElement, Exception> clusterCountFailureReports(String nodeId);

    public Promise<BElement, Exception> rpushx(byte[] key, byte[]... values);

    public Promise<BElement, Exception> pexpireat(byte[] key, long timestamp);

    public Promise<BElement, Exception> flushall();

    public Promise<BElement, Exception> bitopXor(byte[] destination, byte[]... keys);

    public Promise<BElement, Exception> zcount(byte[] key, boolean includeLower, long lower, long upper, boolean includeUpper);

    public Promise<BElement, Exception> hscan(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key, ScanCursor scanCursor, ScanArgs scanArgs);

    public Promise<BElement, Exception> sunionstore(byte[] destination, byte[]... keys);

    public Promise<BElement, Exception> flushallAsync();

    public Promise<BElement, Exception> xread(StreamOffset<byte[]>... streams);

    public Promise<BElement, Exception> flushdb();

    public Promise<BElement, Exception> zincrby(byte[] key, double amount, byte[] member);

    public Promise<BElement, Exception> decr(byte[] key);

    public Promise<BElement, Exception> sscan(byte[] key);

    public Promise<BElement, Exception> clusterKeyslot(byte[] key);

    public Promise<BElement, Exception> flushdbAsync();

    public Promise<BElement, Exception> pttl(byte[] key);

    public Promise<BElement, Exception> xread(XReadArgs args, StreamOffset<byte[]>... streams);

    public Promise<BElement, Exception> info();

    public Promise<BElement, Exception> hscan(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key, ScanCursor scanCursor);

    public Promise<BElement, Exception> sscan(byte[] key, ScanArgs scanArgs);

    public Promise<BElement, Exception> decrby(byte[] key, long amount);

    public Promise<BElement, Exception> randomkey();

    public Promise<BElement, Exception> info(String section);

    public Promise<BElement, Exception> zinterstore(byte[] destination, String aggregate, List<Double> weights, byte[]... keys);

    public Promise<BElement, Exception> sscan(byte[] key, ScanCursor scanCursor, ScanArgs scanArgs);

    public Promise<BElement, Exception> xreadgroup(Consumer<byte[]> consumer, StreamOffset<byte[]>... streams);

    public Promise<BElement, Exception> get(byte[] key);

    public Promise<BElement, Exception> clusterSaveconfig();

    public Promise<BElement, Exception> rename(byte[] key, byte[] newKey);

    public Promise<BElement, Exception> lastsave();

    public Promise<BElement, Exception> hset(byte[] key, byte[] field, byte[] value);

    public Promise<BElement, Exception> getbit(byte[] key, long offset);

    public Promise<BElement, Exception> renamenx(byte[] key, byte[] newKey);

    public Promise<BElement, Exception> clusterSetConfigEpoch(long configEpoch);

    public Promise<BElement, Exception> save();

    public Promise<BElement, Exception> sscan(byte[] key, ScanCursor scanCursor);

    public Promise<BElement, Exception> xreadgroup(Consumer<byte[]> consumer, XReadArgs args, StreamOffset<byte[]>... streams);

    public void shutdown(boolean save);

    public Promise<BElement, Exception> getrange(byte[] key, long start, long end);

    public Promise<BElement, Exception> hsetnx(byte[] key, byte[] field, byte[] value);

    public Promise<BElement, Exception> sscan(ValueStreamingChannel<byte[]> channel, byte[] key);

    public Promise<BElement, Exception> slaveof(String host, int port);

    public Promise<BElement, Exception> clusterSlots();

    public Promise<BElement, Exception> getset(byte[] key, byte[] value);

    public Promise<BElement, Exception> xrevrange(byte[] key, Range<String> range);

    public Promise<BElement, Exception> sscan(ValueStreamingChannel<byte[]> channel, byte[] key, ScanArgs scanArgs);

    public Promise<BElement, Exception> asking();

    public Promise<BElement, Exception> slaveofNoOne();

    public Promise<BElement, Exception> restore(byte[] key, byte[] value, long ttl, boolean replace);

    public Promise<BElement, Exception> zlexcount(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper);

    public Promise<BElement, Exception> slowlogGet();

    public Promise<BElement, Exception> incr(byte[] key);

    public Promise<BElement, Exception> hstrlen(byte[] key, byte[] field);

    public Promise<BElement, Exception> xrevrange(byte[] key, Range<String> range, Limit limit);

    public Promise<BElement, Exception> slowlogGet(int count);

    public Promise<BElement, Exception> clusterReplicate(String nodeId);

    public Promise<BElement, Exception> sscan(ValueStreamingChannel<byte[]> channel, byte[] key, ScanCursor scanCursor, ScanArgs scanArgs);

    public Promise<BElement, Exception> incrby(byte[] key, long amount);

    public Promise<BElement, Exception> zpopmin(byte[] key);

    public Promise<BElement, Exception> slowlogLen();

    public Promise<BElement, Exception> clusterFailover(boolean force);

    public Promise<BElement, Exception> hvals(byte[] key);

    public Promise<BElement, Exception> slowlogReset();

    public Promise<BElement, Exception> incrbyfloat(byte[] key, double amount);

    public Promise<BElement, Exception> xtrim(byte[] key, long count);

    public Promise<BElement, Exception> zpopmin(byte[] key, long count);

    public Promise<BElement, Exception> sscan(ValueStreamingChannel<byte[]> channel, byte[] key, ScanCursor scanCursor);

    public Promise<BElement, Exception> hvals(ValueStreamingChannel<byte[]> channel, byte[] key);

    public Promise<BElement, Exception> time();

    public Promise<BElement, Exception> clusterReset(boolean hard);

    public Promise<BElement, Exception> xtrim(byte[] key, boolean approximateTrimming, long count);

    public Promise<BElement, Exception> sort(java.util.function.Consumer<byte[]> channel, byte[] key, String byPattern, List<String> getPatterns, Long count,
            Long offset, String order, boolean alpha);

    public Promise<BElement, Exception> zpopmax(byte[] key);

    public Promise<BElement, Exception> mget(KeyValueStreamingChannel<byte[], byte[]> channel, byte[]... keys);

    public Promise<BElement, Exception> zpopmax(byte[] key, long count);

    public Promise<BElement, Exception> sortStore(byte[] key, SortArgs sortArgs, byte[] destination);

    public Promise<BElement, Exception> clusterFlushslots();

    public Promise<BElement, Exception> zrange(byte[] key, long start, long stop);

    public Promise<BElement, Exception> touch(byte[]... keys);

    public Promise<BElement, Exception> set(byte[] key, byte[] value);

    public Promise<BElement, Exception> zrange(java.util.function.Consumer<byte[]> valueConsumer, byte[] key, long start, long stop);

    public Promise<BElement, Exception> ttl(byte[] key);

    public Promise<BElement, Exception> del(byte[]... keys);

    public Promise<BElement, Exception> set(byte[] key, byte[] value, SetArgs setArgs);

    public Promise<BElement, Exception> type(byte[] key);

    public Promise<BElement, Exception> zrangeWithScores(byte[] key, long start, long stop);

    public Promise<BElement, Exception> mget(byte[]... keys);

    public Promise<BElement, Exception> setbit(byte[] key, long offset, int value);

    public Promise<BElement, Exception> zrangeWithScores(ScoredValueStreamingChannel<byte[]> channel, byte[] key, long start, long stop);

    public Promise<BElement, Exception> mset(Map<byte[], byte[]> map);

    public Promise<BElement, Exception> setex(byte[] key, long seconds, byte[] value);

    public Promise<BElement, Exception> msetnx(Map<byte[], byte[]> map);

    public Promise<BElement, Exception> psetex(byte[] key, long milliseconds, byte[] value);

    public Promise<BElement, Exception> setnx(byte[] key, byte[] value);

    public Promise<BElement, Exception> zrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper, Long offset,
            Long count);

    public Promise<BElement, Exception> setrange(byte[] key, long offset, byte[] value);

    public Promise<BElement, Exception> strlen(byte[] key);

    public Promise<BElement, Exception> zrangebyscore(java.util.function.Consumer<byte[]> channel, byte[] key, boolean includeLower, long lower, long upper,
            boolean includeUpper, Long offset, Long count);

    public Promise<BElement, Exception> zrangebyscoreWithScores(byte[] key, Range<? extends Number> range);

    public Promise<BElement, Exception> zrangebyscoreWithScores(byte[] key, Range<? extends Number> range, Limit limit);

    public Promise<BElement, Exception> zrangebyscoreWithScores(ScoredValueStreamingChannel<byte[]> channel, byte[] key, Range<? extends Number> range);

    public Promise<BElement, Exception> zrangebyscoreWithScores(ScoredValueStreamingChannel<byte[]> channel, byte[] key, Range<? extends Number> range,
            Limit limit);

    public Promise<BElement, Exception> zrank(byte[] key, byte[] member);

    public Promise<BElement, Exception> zrem(byte[] key, byte[]... members);

    public Promise<BElement, Exception> zremrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper);

    public Promise<BElement, Exception> zremrangebyrank(byte[] key, long start, long stop);

    public Promise<BElement, Exception> zremrangebyscore(byte[] key, boolean includeLower, long lower, long upper, boolean includeUpper);

    public Promise<BElement, Exception> zrevrange(java.util.function.Consumer<byte[]> channel, byte[] key, long start, long stop);

    public Promise<BElement, Exception> zrevrangeWithScores(byte[] key, long start, long stop);

    public Promise<BElement, Exception> zrevrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper, Long offset,
            Long count);

    public Promise<BElement, Exception> zrevrangebyscore(java.util.function.Consumer<byte[]> channel, byte[] key, boolean includeLower, long lower, long upper,
            boolean includeUpper, Long offset, Long count);

    public Promise<BElement, Exception> zrevrank(byte[] key, byte[] member);

    public Promise<BElement, Exception> zscan(BiConsumer<Double, byte[]> channel, byte[] key, String cursor, String match, Long limit);

    public Promise<BElement, Exception> zscore(byte[] key, byte[] member);

    public Promise<BElement, Exception> zunionstore(byte[] destination, String aggregate, List<Double> weights, byte[]... keys);

    public Promise<BElement, Exception> scan(java.util.function.Consumer<byte[]> channel, String cursor, Long count, String match);
}
