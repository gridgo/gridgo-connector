package io.gridgo.connector.redis.adapter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.gridgo.framework.ComponentLifecycle;

import java.util.Map.Entry;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.Client;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.PipelineBlock;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.TransactionBlock;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.JedisCluster.Reset;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.Slowlog;

@SuppressWarnings("deprecation")
public interface RedisClient extends ComponentLifecycle {

	default public Map<String, JedisPool> getClusterNodes() {
		throw new UnsupportedOperationException("unsupported method");
	}

	public String set(String key, String value);

	public String set(String key, String value, String nxxx, String expx, long time);

	public String get(String key);

	public Long exists(String... keys);

	public Boolean exists(String key);

	public Long del(String... keys);

	public Long del(String key);

	public String type(String key);

	public String ping();

	public String set(byte[] key, byte[] value);

	public Set<String> keys(String pattern);

	public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time);

	public byte[] get(byte[] key);

	public String quit();

	public Long exists(byte[]... keys);

	public String randomKey();

	public String rename(String oldkey, String newkey);

	public Boolean exists(byte[] key);

	public Long renamenx(String oldkey, String newkey);

	public Long del(byte[]... keys);

	public Long expire(String key, int seconds);

	public Long del(byte[] key);

	public String type(byte[] key);

	public String flushDB();

	public Set<byte[]> keys(byte[] pattern);

	public Long expireAt(String key, long unixTime);

	public byte[] randomBinaryKey();

	public String rename(byte[] oldkey, byte[] newkey);

	public Long ttl(String key);

	public Long renamenx(byte[] oldkey, byte[] newkey);

	public Long move(String key, int dbIndex);

	public Long dbSize();

	public Long expire(byte[] key, int seconds);

	public String getSet(String key, String value);

	public List<String> mget(String... keys);

	public Long setnx(String key, String value);

	public String setex(String key, int seconds, String value);

	public String mset(String... keysvalues);

	public Long expireAt(byte[] key, long unixTime);

	public Long msetnx(String... keysvalues);

	public Long decrBy(String key, long integer);

	public Long ttl(byte[] key);

	public Long decr(String key);

	public String select(int index);

	public Long move(byte[] key, int dbIndex);

	public Long incrBy(String key, long integer);

	public String flushAll();

	public byte[] getSet(byte[] key, byte[] value);

	public Double incrByFloat(String key, double value);

	public List<byte[]> mget(byte[]... keys);

	public Long setnx(byte[] key, byte[] value);

	public Long incr(String key);

	public String setex(byte[] key, int seconds, byte[] value);

	public Long append(String key, String value);

	public String mset(byte[]... keysvalues);

	public String substr(String key, int start, int end);

	public Long msetnx(byte[]... keysvalues);

	public Long hset(String key, String field, String value);

	public Long decrBy(byte[] key, long integer);

	public String hget(String key, String field);

	public Long hsetnx(String key, String field, String value);

	public Long decr(byte[] key);

	public String hmset(String key, Map<String, String> hash);

	public Long incrBy(byte[] key, long integer);

	public List<String> hmget(String key, String... fields);

	public Long hincrBy(String key, String field, long value);

	public Double incrByFloat(byte[] key, double integer);

	public Double hincrByFloat(String key, String field, double value);

	public Long incr(byte[] key);

	public Boolean hexists(String key, String field);

	public Long append(byte[] key, byte[] value);

	public Long hdel(String key, String... fields);

	public Long hlen(String key);

	public byte[] substr(byte[] key, int start, int end);

	public Set<String> hkeys(String key);

	public List<String> hvals(String key);

	public Map<String, String> hgetAll(String key);

	public Long hset(byte[] key, byte[] field, byte[] value);

	public Long rpush(String key, String... strings);

	public byte[] hget(byte[] key, byte[] field);

	public Long lpush(String key, String... strings);

	public Long hsetnx(byte[] key, byte[] field, byte[] value);

	public String hmset(byte[] key, Map<byte[], byte[]> hash);

	public Long llen(String key);

	public List<byte[]> hmget(byte[] key, byte[]... fields);

	public List<String> lrange(String key, long start, long end);

	public Long hincrBy(byte[] key, byte[] field, long value);

	public Double hincrByFloat(byte[] key, byte[] field, double value);

	public String ltrim(String key, long start, long end);

	public Boolean hexists(byte[] key, byte[] field);

	public Long hdel(byte[] key, byte[]... fields);

	public Long hlen(byte[] key);

	public String lindex(String key, long index);

	public Set<byte[]> hkeys(byte[] key);

	public List<byte[]> hvals(byte[] key);

	public String lset(String key, long index, String value);

	public Map<byte[], byte[]> hgetAll(byte[] key);

	public Long lrem(String key, long count, String value);

	public Long rpush(byte[] key, byte[]... strings);

	public Long lpush(byte[] key, byte[]... strings);

	public String lpop(String key);

	public Long llen(byte[] key);

	public String rpop(String key);

	public List<byte[]> lrange(byte[] key, long start, long end);

	public String rpoplpush(String srckey, String dstkey);

	public Long sadd(String key, String... members);

	public String ltrim(byte[] key, long start, long end);

	public Set<String> smembers(String key);

	public Long srem(String key, String... members);

	public String spop(String key);

	public byte[] lindex(byte[] key, long index);

	public Set<String> spop(String key, long count);

	public Long smove(String srckey, String dstkey, String member);

	public String lset(byte[] key, long index, byte[] value);

	public Long scard(String key);

	public Long lrem(byte[] key, long count, byte[] value);

	public Boolean sismember(String key, String member);

	public Set<String> sinter(String... keys);

	public byte[] lpop(byte[] key);

	public byte[] rpop(byte[] key);

	public Long sinterstore(String dstkey, String... keys);

	public byte[] rpoplpush(byte[] srckey, byte[] dstkey);

	public Set<String> sunion(String... keys);

	public Long sadd(byte[] key, byte[]... members);

	public Long sunionstore(String dstkey, String... keys);

	public Set<byte[]> smembers(byte[] key);

	public Set<String> sdiff(String... keys);

	public Long srem(byte[] key, byte[]... member);

	public Long sdiffstore(String dstkey, String... keys);

	public byte[] spop(byte[] key);

	public String srandmember(String key);

	public Set<byte[]> spop(byte[] key, long count);

	public Long smove(byte[] srckey, byte[] dstkey, byte[] member);

	public List<String> srandmember(String key, int count);

	public Long zadd(String key, double score, String member);

	public Long scard(byte[] key);

	public Long zadd(String key, double score, String member, ZAddParams params);

	public Boolean sismember(byte[] key, byte[] member);

	public Long zadd(String key, Map<String, Double> scoreMembers);

	public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params);

	public Set<String> zrange(String key, long start, long end);

	public Set<byte[]> sinter(byte[]... keys);

	public Long zrem(String key, String... members);

	public Double zincrby(String key, double score, String member);

	public Long sinterstore(byte[] dstkey, byte[]... keys);

	public Set<byte[]> sunion(byte[]... keys);

	public Double zincrby(String key, double score, String member, ZIncrByParams params);

	public Long sunionstore(byte[] dstkey, byte[]... keys);

	public Long zrank(String key, String member);

	public Set<byte[]> sdiff(byte[]... keys);

	public Long zrevrank(String key, String member);

	public Long sdiffstore(byte[] dstkey, byte[]... keys);

	public Set<String> zrevrange(String key, long start, long end);

	public byte[] srandmember(byte[] key);

	public Set<Tuple> zrangeWithScores(String key, long start, long end);

	public Set<Tuple> zrevrangeWithScores(String key, long start, long end);

	public List<byte[]> srandmember(byte[] key, int count);

	public Long zcard(String key);

	public Long zadd(byte[] key, double score, byte[] member);

	public Double zscore(String key, String member);

	public String watch(String... keys);

	public List<String> sort(String key);

	public Long zadd(byte[] key, double score, byte[] member, ZAddParams params);

	public Long zadd(byte[] key, Map<byte[], Double> scoreMembers);

	public Long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params);

	public Set<byte[]> zrange(byte[] key, long start, long end);

	public List<String> sort(String key, SortingParams sortingParameters);

	public Long zrem(byte[] key, byte[]... members);

	public Double zincrby(byte[] key, double score, byte[] member);

	public List<String> blpop(int timeout, String... keys);

	public Double zincrby(byte[] key, double score, byte[] member, ZIncrByParams params);

	public Long zrank(byte[] key, byte[] member);

	public Long zrevrank(byte[] key, byte[] member);

	public Set<byte[]> zrevrange(byte[] key, long start, long end);

	public Set<Tuple> zrangeWithScores(byte[] key, long start, long end);

	public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long end);

	public Long zcard(byte[] key);

	public Double zscore(byte[] key, byte[] member);

	public List<String> blpop(String... args);

	public List<String> brpop(String... args);

	public Transaction multi();

	public List<Object> multi(TransactionBlock jedisTransaction);

	public List<String> blpop(String arg);

	public List<String> brpop(String arg);

	public Long sort(String key, SortingParams sortingParameters, String dstkey);

	public Long sort(String key, String dstkey);

	public String watch(byte[]... keys);

	public String unwatch();

	public List<byte[]> sort(byte[] key);

	public List<String> brpop(int timeout, String... keys);

	public List<byte[]> sort(byte[] key, SortingParams sortingParameters);

	public List<byte[]> blpop(int timeout, byte[]... keys);

	public Long zcount(String key, double min, double max);

	public Long zcount(String key, String min, String max);

	public Set<String> zrangeByScore(String key, double min, double max);

	public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey);

	public Long sort(byte[] key, byte[] dstkey);

	public Set<String> zrangeByScore(String key, String min, String max);

	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count);

	public List<byte[]> brpop(int timeout, byte[]... keys);

	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count);

	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max);

	public List<byte[]> blpop(byte[] arg);

	public List<byte[]> brpop(byte[] arg);

	public List<byte[]> blpop(byte[]... args);

	public List<byte[]> brpop(byte[]... args);

	public String auth(String password);

	public List<Object> pipelined(PipelineBlock jedisPipeline);

	public Pipeline pipelined();

	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max);

	public Long zcount(byte[] key, double min, double max);

	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count);

	public Long zcount(byte[] key, byte[] min, byte[] max);

	public Set<byte[]> zrangeByScore(byte[] key, double min, double max);

	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count);

	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max);

	public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count);

	public Set<String> zrevrangeByScore(String key, double max, double min);

	public Set<String> zrevrangeByScore(String key, String max, String min);

	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count);

	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min);

	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count);

	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count);

	public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count);

	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count);

	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min);

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max);

	public Long zremrangeByRank(String key, long start, long end);

	public Long zremrangeByScore(String key, double start, double end);

	public Long zremrangeByScore(String key, String start, String end);

	public Long zunionstore(String dstkey, String... sets);

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max);

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count);

	public Long zunionstore(String dstkey, ZParams params, String... sets);

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count);

	public Long zinterstore(String dstkey, String... sets);

	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min);

	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min);

	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count);

	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count);

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min);

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count);

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min);

	public Long zinterstore(String dstkey, ZParams params, String... sets);

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count);

	public Long zremrangeByRank(byte[] key, long start, long end);

	public Long zremrangeByScore(byte[] key, double start, double end);

	public Long zremrangeByScore(byte[] key, byte[] start, byte[] end);

	public Long zlexcount(String key, String min, String max);

	public Long zunionstore(byte[] dstkey, byte[]... sets);

	public Set<String> zrangeByLex(String key, String min, String max);

	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count);

	public Set<String> zrevrangeByLex(String key, String max, String min);

	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count);

	public Long zremrangeByLex(String key, String min, String max);

	public Long strlen(String key);

	public Long lpushx(String key, String... string);

	public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets);

	public Long persist(String key);

	public Long rpushx(String key, String... string);

	public String echo(String string);

	public Long linsert(String key, LIST_POSITION where, String pivot, String value);

	public String brpoplpush(String source, String destination, int timeout);

	public Boolean setbit(String key, long offset, boolean value);

	public Boolean setbit(String key, long offset, String value);

	public Boolean getbit(String key, long offset);

	public Long zinterstore(byte[] dstkey, byte[]... sets);

	public Long setrange(String key, long offset, String value);

	public String getrange(String key, long startOffset, long endOffset);

	public Long bitpos(String key, boolean value);

	public Long bitpos(String key, boolean value, BitPosParams params);

	public List<String> configGet(String pattern);

	public String configSet(String parameter, String value);

	public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets);

	public Object eval(String script, int keyCount, String... params);

	/**
	 * subscribe to a topic
	 * 
	 * @param jedisPubSub
	 * @param channels
	 */
	public void subscribe(JedisPubSub jedisPubSub, String... channels);

	public Long publish(String channel, String message);

	public Long zlexcount(byte[] key, byte[] min, byte[] max);

	public void psubscribe(JedisPubSub jedisPubSub, String... patterns);

	public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max);

	public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count);

	public Object eval(String script, List<String> keys, List<String> args);

	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min);

	public Object eval(String script);

	public Object evalsha(String script);

	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count);

	public Long zremrangeByLex(byte[] key, byte[] min, byte[] max);

	public String save();

	public Object evalsha(String sha1, List<String> keys, List<String> args);

	public Object evalsha(String sha1, int keyCount, String... params);

	public Boolean scriptExists(String sha1);

	public List<Boolean> scriptExists(String... sha1);

	public String bgsave();

	public String scriptLoad(String script);

	public List<Slowlog> slowlogGet();

	public List<Slowlog> slowlogGet(long entries);

	public String bgrewriteaof();

	public Long objectRefcount(String string);

	public String objectEncoding(String string);

	public Long objectIdletime(String string);

	public Long bitcount(String key);

	public Long bitcount(String key, long start, long end);

	public Long bitop(BitOP op, String destKey, String... srcKeys);

	public List<Map<String, String>> sentinelMasters();

	public Long lastsave();

	public List<String> sentinelGetMasterAddrByName(String masterName);

	public String info();

	public Long sentinelReset(String pattern);

	public List<Map<String, String>> sentinelSlaves(String masterName);

	public String info(String section);

	public void monitor(JedisMonitor jedisMonitor);

	public String sentinelFailover(String masterName);

	public String sentinelMonitor(String masterName, String ip, int port, int quorum);

	public String slaveof(String host, int port);

	public String sentinelRemove(String masterName);

	public String sentinelSet(String masterName, Map<String, String> parameterMap);

	public byte[] dump(String key);

	public String restore(String key, int ttl, byte[] serializedValue);

	public Long pexpire(String key, int milliseconds);

	public String slaveofNoOne();

	public Long pexpire(String key, long milliseconds);

	public List<byte[]> configGet(byte[] pattern);

	public Long pexpireAt(String key, long millisecondsTimestamp);

	public Long pttl(String key);

	public String psetex(String key, int milliseconds, String value);

	public String psetex(String key, long milliseconds, String value);

	public String configResetStat();

	public String set(String key, String value, String nxxx);

	public byte[] configSet(byte[] parameter, byte[] value);

	public String set(String key, String value, String nxxx, String expx, int time);

	public String clientKill(String client);

	public String clientSetname(String name);

	public String migrate(String host, int port, String key, int destinationDb, int timeout);

	public ScanResult<String> scan(int cursor);

	public ScanResult<String> scan(int cursor, ScanParams params);

	public Long strlen(byte[] key);

	public void sync();

	public Long lpushx(byte[] key, byte[]... string);

	public Long persist(byte[] key);

	public ScanResult<Entry<String, String>> hscan(String key, int cursor);

	public Long rpushx(byte[] key, byte[]... string);

	public ScanResult<Entry<String, String>> hscan(String key, int cursor, ScanParams params);

	public byte[] echo(byte[] string);

	public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot, byte[] value);

	public String debug(DebugParams params);

	public Client getClient();

	public byte[] brpoplpush(byte[] source, byte[] destination, int timeout);

	public ScanResult<String> sscan(String key, int cursor);

	public Boolean setbit(byte[] key, long offset, boolean value);

	public ScanResult<String> sscan(String key, int cursor, ScanParams params);

	public Boolean setbit(byte[] key, long offset, byte[] value);

	public Boolean getbit(byte[] key, long offset);

	public Long bitpos(byte[] key, boolean value);

	public Long bitpos(byte[] key, boolean value, BitPosParams params);

	public Long setrange(byte[] key, long offset, byte[] value);

	public ScanResult<Tuple> zscan(String key, int cursor);

	public byte[] getrange(byte[] key, long startOffset, long endOffset);

	public Long publish(byte[] channel, byte[] message);

	public ScanResult<Tuple> zscan(String key, int cursor, ScanParams params);

	public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels);

	public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns);

	public Long getDB();

	public byte[] scriptLoad(byte[] script, byte[] key);

	public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args);

	public Object eval(byte[] script, byte[] key);

	public Object eval(byte[] script, byte[] keyCount, byte[]... params);

	public Object eval(byte[] script, int keyCount, byte[]... params);

	public ScanResult<String> scan(String cursor);

	public ScanResult<String> scan(String cursor, ScanParams params);

	public ScanResult<Entry<String, String>> hscan(String key, String cursor);

	public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params);

	public Object eval(byte[] script);

	public Object evalsha(byte[] sha1);

	public Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args);

	public Object evalsha(byte[] sha1, int keyCount, byte[]... params);

	public String scriptFlush();

	public Long scriptExists(byte[] sha1);

	public List<Long> scriptExists(byte[]... sha1);

	default public String scriptFlush(byte[] key) {
		throw new UnsupportedOperationException("unsupported method");
	}

	default public String scriptKill(byte[] key) {
		throw new UnsupportedOperationException("unsupported method");
	}

	public ScanResult<String> sscan(String key, String cursor);

	public ScanResult<String> sscan(String key, String cursor, ScanParams params);

	public byte[] scriptLoad(byte[] script);

	public String scriptKill();

	public String slowlogReset();

	public Long slowlogLen();

	public List<byte[]> slowlogGetBinary();

	public List<byte[]> slowlogGetBinary(long entries);

	public ScanResult<Tuple> zscan(String key, String cursor);

	public Long objectRefcount(byte[] key);

	public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params);

	public byte[] objectEncoding(byte[] key);

	public Long objectIdletime(byte[] key);

	public Long bitcount(byte[] key);

	public Long bitcount(byte[] key, long start, long end);

	public Long bitop(BitOP op, byte[] destKey, byte[]... srcKeys);

	public String clusterNodes();

	public byte[] dump(byte[] key);

	public String readonly();

	public String restore(byte[] key, int ttl, byte[] serializedValue);

	public String clusterMeet(String ip, int port);

	public String clusterReset(Reset resetType);

	public Long pexpire(byte[] key, int milliseconds);

	public Long pexpire(byte[] key, long milliseconds);

	public String clusterAddSlots(int... slots);

	public String clusterDelSlots(int... slots);

	public Long pexpireAt(byte[] key, long millisecondsTimestamp);

	public String clusterInfo();

	public Long pttl(byte[] key);

	public List<String> clusterGetKeysInSlot(int slot, int count);

	public String psetex(byte[] key, int milliseconds, byte[] value);

	public String psetex(byte[] key, long milliseconds, byte[] value);

	public String clusterSetSlotNode(int slot, String nodeId);

	public String clusterSetSlotMigrating(int slot, String nodeId);

	public String clusterSetSlotImporting(int slot, String nodeId);

	public String set(byte[] key, byte[] value, byte[] nxxx);

	public String clusterSetSlotStable(int slot);

	public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, int time);

	public String clusterForget(String nodeId);

	public String clientKill(byte[] client);

	public String clusterFlushSlots();

	public Long clusterKeySlot(String key);

	public String clientGetname();

	public String clientList();

	public Long clusterCountKeysInSlot(int slot);

	public String clientSetname(byte[] name);

	public String clusterSaveConfig();

	public List<String> time();

	public String clusterReplicate(String nodeId);

	public String migrate(byte[] host, int port, byte[] key, int destinationDb, int timeout);

	public List<String> clusterSlaves(String nodeId);

	public String clusterFailover();

	public Long waitReplicas(int replicas, long timeout);

	public List<Object> clusterSlots();

	public String asking();

	public Long pfadd(byte[] key, byte[]... elements);

	public List<String> pubsubChannels(String pattern);

	public long pfcount(byte[] key);

	public Long pubsubNumPat();

	public Map<String, String> pubsubNumSub(String... channels);

	public String pfmerge(byte[] destkey, byte[]... sourcekeys);

	public Long pfcount(byte[]... keys);

	public ScanResult<byte[]> scan(byte[] cursor);

	public ScanResult<byte[]> scan(byte[] cursor, ScanParams params);

	public Long pfadd(String key, String... elements);

	public long pfcount(String key);

	public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor);

	public long pfcount(String... keys);

	public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params);

	public String pfmerge(String destkey, String... sourcekeys);

	public List<String> blpop(int timeout, String key);

	public List<String> brpop(int timeout, String key);

	public Long geoadd(String key, double longitude, double latitude, String member);

	public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap);

	public ScanResult<byte[]> sscan(byte[] key, byte[] cursor);

	public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params);

	public Double geodist(String key, String member1, String member2);

	public Double geodist(String key, String member1, String member2, GeoUnit unit);

	public ScanResult<Tuple> zscan(byte[] key, byte[] cursor);

	public List<String> geohash(String key, String... members);

	public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params);

	public List<GeoCoordinate> geopos(String key, String... members);

	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius,
			GeoUnit unit);

	public Long geoadd(byte[] key, double longitude, double latitude, byte[] member);

	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param);

	public Long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap);

	public Double geodist(byte[] key, byte[] member1, byte[] member2);

	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit);

	public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit);

	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
			GeoRadiusParam param);

	public List<byte[]> geohash(byte[] key, byte[]... members);

	public List<Long> bitfield(String key, String... arguments);

	public List<GeoCoordinate> geopos(byte[] key, byte[]... members);

	public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius,
			GeoUnit unit);

	public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param);

	public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit);

	public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit,
			GeoRadiusParam param);

	public List<byte[]> bitfield(byte[] key, byte[]... arguments);
}
