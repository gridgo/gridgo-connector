package io.gridgo.connector.redis.adapter.jedis.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import io.gridgo.connector.redis.adapter.RedisConfig;
import io.gridgo.connector.redis.adapter.jedis.JedisClient;
import lombok.AccessLevel;
import lombok.Setter;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.Client;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster.Reset;
import redis.clients.jedis.JedisMonitor;
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
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.Pool;
import redis.clients.util.Slowlog;

@SuppressWarnings("deprecation")
public abstract class PooledJedisClient extends JedisClient {

	@Setter(AccessLevel.PROTECTED)
	private Pool<Jedis> pool;

	protected PooledJedisClient(RedisConfig config) {
		super(config);
	}

	@Override
	protected void onStop() {
		this.pool.close();
	}

	protected Jedis getJedis() {
		if (!this.isStarted()) {
			throw new IllegalStateException("a PooledJedisClient must be started before use");
		}
		return this.pool.getResource();
	}

	protected final <T> T call(Function<Jedis, T> function) {
		try (Jedis jedis = this.getJedis()) {
			return function.apply(jedis);
		}
	}

	protected final void execute(Consumer<Jedis> consumer) {
		try (Jedis jedis = this.getJedis()) {
			consumer.accept(jedis);
		}
	}

	public String set(String key, String value) {
		return call(jedis -> {
			return jedis.set(key, value);
		});
	}

	public String set(String key, String value, String nxxx, String expx, long time) {
		return call(jedis -> {
			return jedis.set(key, value, nxxx, expx, time);
		});
	}

	public String get(String key) {
		return call(jedis -> {
			return jedis.get(key);
		});
	}

	public Long exists(String... keys) {
		return call(jedis -> {
			return jedis.exists(keys);
		});
	}

	public Boolean exists(String key) {
		return call(jedis -> {
			return jedis.exists(key);
		});
	}

	public Long del(String... keys) {
		return call(jedis -> {
			return jedis.del(keys);
		});
	}

	public Long del(String key) {
		return call(jedis -> {
			return jedis.del(key);
		});
	}

	public String type(String key) {
		return call(jedis -> {
			return jedis.type(key);
		});
	}

	public String ping() {
		return call(jedis -> {
			return jedis.ping();
		});
	}

	public String set(byte[] key, byte[] value) {
		return call(jedis -> {
			return jedis.set(key, value);
		});
	}

	public Set<String> keys(String pattern) {
		return call(jedis -> {
			return jedis.keys(pattern);
		});
	}

	public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time) {
		return call(jedis -> {
			return jedis.set(key, value, nxxx, expx, time);
		});
	}

	public byte[] get(byte[] key) {
		return call(jedis -> {
			return jedis.get(key);
		});
	}

	public String quit() {
		return call(jedis -> {
			return jedis.quit();
		});
	}

	public Long exists(byte[]... keys) {
		return call(jedis -> {
			return jedis.exists(keys);
		});
	}

	public String randomKey() {
		return call(jedis -> {
			return jedis.randomKey();
		});
	}

	public String rename(String oldkey, String newkey) {
		return call(jedis -> {
			return jedis.rename(oldkey, newkey);
		});
	}

	public Boolean exists(byte[] key) {
		return call(jedis -> {
			return jedis.exists(key);
		});
	}

	public Long renamenx(String oldkey, String newkey) {
		return call(jedis -> {
			return jedis.renamenx(oldkey, newkey);
		});
	}

	public Long del(byte[]... keys) {
		return call(jedis -> {
			return jedis.del(keys);
		});
	}

	public Long expire(String key, int seconds) {
		return call(jedis -> {
			return jedis.expire(key, seconds);
		});
	}

	public Long del(byte[] key) {
		return call(jedis -> {
			return jedis.del(key);
		});
	}

	public String type(byte[] key) {
		return call(jedis -> {
			return jedis.type(key);
		});
	}

	public String flushDB() {
		return call(jedis -> {
			return jedis.flushDB();
		});
	}

	public Set<byte[]> keys(byte[] pattern) {
		return call(jedis -> {
			return jedis.keys(pattern);
		});
	}

	public Long expireAt(String key, long unixTime) {
		return call(jedis -> {
			return jedis.expireAt(key, unixTime);
		});
	}

	public byte[] randomBinaryKey() {
		return call(jedis -> {
			return jedis.randomBinaryKey();
		});
	}

	public String rename(byte[] oldkey, byte[] newkey) {
		return call(jedis -> {
			return jedis.rename(oldkey, newkey);
		});
	}

	public Long ttl(String key) {
		return call(jedis -> {
			return jedis.ttl(key);
		});
	}

	public Long renamenx(byte[] oldkey, byte[] newkey) {
		return call(jedis -> {
			return jedis.renamenx(oldkey, newkey);
		});
	}

	public Long move(String key, int dbIndex) {
		return call(jedis -> {
			return jedis.move(key, dbIndex);
		});
	}

	public Long dbSize() {
		return call(jedis -> {
			return jedis.dbSize();
		});
	}

	public Long expire(byte[] key, int seconds) {
		return call(jedis -> {
			return jedis.expire(key, seconds);
		});
	}

	public String getSet(String key, String value) {
		return call(jedis -> {
			return jedis.getSet(key, value);
		});
	}

	public List<String> mget(String... keys) {
		return call(jedis -> {
			return jedis.mget(keys);
		});
	}

	public Long setnx(String key, String value) {
		return call(jedis -> {
			return jedis.setnx(key, value);
		});
	}

	public String setex(String key, int seconds, String value) {
		return call(jedis -> {
			return jedis.setex(key, seconds, value);
		});
	}

	public String mset(String... keysvalues) {
		return call(jedis -> {
			return jedis.mset(keysvalues);
		});
	}

	public Long expireAt(byte[] key, long unixTime) {
		return call(jedis -> {
			return jedis.expireAt(key, unixTime);
		});
	}

	public Long msetnx(String... keysvalues) {
		return call(jedis -> {
			return jedis.msetnx(keysvalues);
		});
	}

	public Long decrBy(String key, long integer) {
		return call(jedis -> {
			return jedis.decrBy(key, integer);
		});
	}

	public Long ttl(byte[] key) {
		return call(jedis -> {
			return jedis.ttl(key);
		});
	}

	public Long decr(String key) {
		return call(jedis -> {
			return jedis.decr(key);
		});
	}

	public String select(int index) {
		return call(jedis -> {
			return jedis.select(index);
		});
	}

	public Long move(byte[] key, int dbIndex) {
		return call(jedis -> {
			return jedis.move(key, dbIndex);
		});
	}

	public Long incrBy(String key, long integer) {
		return call(jedis -> {
			return jedis.incrBy(key, integer);
		});
	}

	public String flushAll() {
		return call(jedis -> {
			return jedis.flushAll();
		});
	}

	public byte[] getSet(byte[] key, byte[] value) {
		return call(jedis -> {
			return jedis.getSet(key, value);
		});
	}

	public Double incrByFloat(String key, double value) {
		return call(jedis -> {
			return jedis.incrByFloat(key, value);
		});
	}

	public List<byte[]> mget(byte[]... keys) {
		return call(jedis -> {
			return jedis.mget(keys);
		});
	}

	public Long setnx(byte[] key, byte[] value) {
		return call(jedis -> {
			return jedis.setnx(key, value);
		});
	}

	public Long incr(String key) {
		return call(jedis -> {
			return jedis.incr(key);
		});
	}

	public String setex(byte[] key, int seconds, byte[] value) {
		return call(jedis -> {
			return jedis.setex(key, seconds, value);
		});
	}

	public Long append(String key, String value) {
		return call(jedis -> {
			return jedis.append(key, value);
		});
	}

	public String mset(byte[]... keysvalues) {
		return call(jedis -> {
			return jedis.mset(keysvalues);
		});
	}

	public String substr(String key, int start, int end) {
		return call(jedis -> {
			return jedis.substr(key, start, end);
		});
	}

	public Long msetnx(byte[]... keysvalues) {
		return call(jedis -> {
			return jedis.msetnx(keysvalues);
		});
	}

	public Long hset(String key, String field, String value) {
		return call(jedis -> {
			return jedis.hset(key, field, value);
		});
	}

	public Long decrBy(byte[] key, long integer) {
		return call(jedis -> {
			return jedis.decrBy(key, integer);
		});
	}

	public String hget(String key, String field) {
		return call(jedis -> {
			return jedis.hget(key, field);
		});
	}

	public Long hsetnx(String key, String field, String value) {
		return call(jedis -> {
			return jedis.hsetnx(key, field, value);
		});
	}

	public Long decr(byte[] key) {
		return call(jedis -> {
			return jedis.decr(key);
		});
	}

	public String hmset(String key, Map<String, String> hash) {
		return call(jedis -> {
			return jedis.hmset(key, hash);
		});
	}

	public Long incrBy(byte[] key, long integer) {
		return call(jedis -> {
			return jedis.incrBy(key, integer);
		});
	}

	public List<String> hmget(String key, String... fields) {
		return call(jedis -> {
			return jedis.hmget(key, fields);
		});
	}

	public Long hincrBy(String key, String field, long value) {
		return call(jedis -> {
			return jedis.hincrBy(key, field, value);
		});
	}

	public Double incrByFloat(byte[] key, double integer) {
		return call(jedis -> {
			return jedis.incrByFloat(key, integer);
		});
	}

	public Double hincrByFloat(String key, String field, double value) {
		return call(jedis -> {
			return jedis.hincrByFloat(key, field, value);
		});
	}

	public Long incr(byte[] key) {
		return call(jedis -> {
			return jedis.incr(key);
		});
	}

	public Boolean hexists(String key, String field) {
		return call(jedis -> {
			return jedis.hexists(key, field);
		});
	}

	public Long append(byte[] key, byte[] value) {
		return call(jedis -> {
			return jedis.append(key, value);
		});
	}

	public Long hdel(String key, String... fields) {
		return call(jedis -> {
			return jedis.hdel(key, fields);
		});
	}

	public Long hlen(String key) {
		return call(jedis -> {
			return jedis.hlen(key);
		});
	}

	public byte[] substr(byte[] key, int start, int end) {
		return call(jedis -> {
			return jedis.substr(key, start, end);
		});
	}

	public Set<String> hkeys(String key) {
		return call(jedis -> {
			return jedis.hkeys(key);
		});
	}

	public List<String> hvals(String key) {
		return call(jedis -> {
			return jedis.hvals(key);
		});
	}

	public Map<String, String> hgetAll(String key) {
		return call(jedis -> {
			return jedis.hgetAll(key);
		});
	}

	public Long hset(byte[] key, byte[] field, byte[] value) {
		return call(jedis -> {
			return jedis.hset(key, field, value);
		});
	}

	public Long rpush(String key, String... strings) {
		return call(jedis -> {
			return jedis.rpush(key, strings);
		});
	}

	public byte[] hget(byte[] key, byte[] field) {
		return call(jedis -> {
			return jedis.hget(key, field);
		});
	}

	public Long lpush(String key, String... strings) {
		return call(jedis -> {
			return jedis.lpush(key, strings);
		});
	}

	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		return call(jedis -> {
			return jedis.hsetnx(key, field, value);
		});
	}

	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		return call(jedis -> {
			return jedis.hmset(key, hash);
		});
	}

	public Long llen(String key) {
		return call(jedis -> {
			return jedis.llen(key);
		});
	}

	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		return call(jedis -> {
			return jedis.hmget(key, fields);
		});
	}

	public List<String> lrange(String key, long start, long end) {
		return call(jedis -> {
			return jedis.lrange(key, start, end);
		});
	}

	public Long hincrBy(byte[] key, byte[] field, long value) {
		return call(jedis -> {
			return jedis.hincrBy(key, field, value);
		});
	}

	public Double hincrByFloat(byte[] key, byte[] field, double value) {
		return call(jedis -> {
			return jedis.hincrByFloat(key, field, value);
		});
	}

	public String ltrim(String key, long start, long end) {
		return call(jedis -> {
			return jedis.ltrim(key, start, end);
		});
	}

	public Boolean hexists(byte[] key, byte[] field) {
		return call(jedis -> {
			return jedis.hexists(key, field);
		});
	}

	public Long hdel(byte[] key, byte[]... fields) {
		return call(jedis -> {
			return jedis.hdel(key, fields);
		});
	}

	public Long hlen(byte[] key) {
		return call(jedis -> {
			return jedis.hlen(key);
		});
	}

	public String lindex(String key, long index) {
		return call(jedis -> {
			return jedis.lindex(key, index);
		});
	}

	public Set<byte[]> hkeys(byte[] key) {
		return call(jedis -> {
			return jedis.hkeys(key);
		});
	}

	public List<byte[]> hvals(byte[] key) {
		return call(jedis -> {
			return jedis.hvals(key);
		});
	}

	public String lset(String key, long index, String value) {
		return call(jedis -> {
			return jedis.lset(key, index, value);
		});
	}

	public Map<byte[], byte[]> hgetAll(byte[] key) {
		return call(jedis -> {
			return jedis.hgetAll(key);
		});
	}

	public Long lrem(String key, long count, String value) {
		return call(jedis -> {
			return jedis.lrem(key, count, value);
		});
	}

	public Long rpush(byte[] key, byte[]... strings) {
		return call(jedis -> {
			return jedis.rpush(key, strings);
		});
	}

	public Long lpush(byte[] key, byte[]... strings) {
		return call(jedis -> {
			return jedis.lpush(key, strings);
		});
	}

	public String lpop(String key) {
		return call(jedis -> {
			return jedis.lpop(key);
		});
	}

	public Long llen(byte[] key) {
		return call(jedis -> {
			return jedis.llen(key);
		});
	}

	public String rpop(String key) {
		return call(jedis -> {
			return jedis.rpop(key);
		});
	}

	public List<byte[]> lrange(byte[] key, long start, long end) {
		return call(jedis -> {
			return jedis.lrange(key, start, end);
		});
	}

	public String rpoplpush(String srckey, String dstkey) {
		return call(jedis -> {
			return jedis.rpoplpush(srckey, dstkey);
		});
	}

	public Long sadd(String key, String... members) {
		return call(jedis -> {
			return jedis.sadd(key, members);
		});
	}

	public String ltrim(byte[] key, long start, long end) {
		return call(jedis -> {
			return jedis.ltrim(key, start, end);
		});
	}

	public Set<String> smembers(String key) {
		return call(jedis -> {
			return jedis.smembers(key);
		});
	}

	public Long srem(String key, String... members) {
		return call(jedis -> {
			return jedis.srem(key, members);
		});
	}

	public String spop(String key) {
		return call(jedis -> {
			return jedis.spop(key);
		});
	}

	public byte[] lindex(byte[] key, long index) {
		return call(jedis -> {
			return jedis.lindex(key, index);
		});
	}

	public Set<String> spop(String key, long count) {
		return call(jedis -> {
			return jedis.spop(key, count);
		});
	}

	public Long smove(String srckey, String dstkey, String member) {
		return call(jedis -> {
			return jedis.smove(srckey, dstkey, member);
		});
	}

	public String lset(byte[] key, long index, byte[] value) {
		return call(jedis -> {
			return jedis.lset(key, index, value);
		});
	}

	public Long scard(String key) {
		return call(jedis -> {
			return jedis.scard(key);
		});
	}

	public Long lrem(byte[] key, long count, byte[] value) {
		return call(jedis -> {
			return jedis.lrem(key, count, value);
		});
	}

	public Boolean sismember(String key, String member) {
		return call(jedis -> {
			return jedis.sismember(key, member);
		});
	}

	public Set<String> sinter(String... keys) {
		return call(jedis -> {
			return jedis.sinter(keys);
		});
	}

	public byte[] lpop(byte[] key) {
		return call(jedis -> {
			return jedis.lpop(key);
		});
	}

	public byte[] rpop(byte[] key) {
		return call(jedis -> {
			return jedis.rpop(key);
		});
	}

	public Long sinterstore(String dstkey, String... keys) {
		return call(jedis -> {
			return jedis.sinterstore(dstkey, keys);
		});
	}

	public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
		return call(jedis -> {
			return jedis.rpoplpush(srckey, dstkey);
		});
	}

	public Set<String> sunion(String... keys) {
		return call(jedis -> {
			return jedis.sunion(keys);
		});
	}

	public Long sadd(byte[] key, byte[]... members) {
		return call(jedis -> {
			return jedis.sadd(key, members);
		});
	}

	public Long sunionstore(String dstkey, String... keys) {
		return call(jedis -> {
			return jedis.sunionstore(dstkey, keys);
		});
	}

	public Set<byte[]> smembers(byte[] key) {
		return call(jedis -> {
			return jedis.smembers(key);
		});
	}

	public Set<String> sdiff(String... keys) {
		return call(jedis -> {
			return jedis.sdiff(keys);
		});
	}

	public Long srem(byte[] key, byte[]... member) {
		return call(jedis -> {
			return jedis.srem(key, member);
		});
	}

	public Long sdiffstore(String dstkey, String... keys) {
		return call(jedis -> {
			return jedis.sdiffstore(dstkey, keys);
		});
	}

	public byte[] spop(byte[] key) {
		return call(jedis -> {
			return jedis.spop(key);
		});
	}

	public String srandmember(String key) {
		return call(jedis -> {
			return jedis.srandmember(key);
		});
	}

	public Set<byte[]> spop(byte[] key, long count) {
		return call(jedis -> {
			return jedis.spop(key, count);
		});
	}

	public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
		return call(jedis -> {
			return jedis.smove(srckey, dstkey, member);
		});
	}

	public List<String> srandmember(String key, int count) {
		return call(jedis -> {
			return jedis.srandmember(key, count);
		});
	}

	public Long zadd(String key, double score, String member) {
		return call(jedis -> {
			return jedis.zadd(key, score, member);
		});
	}

	public Long scard(byte[] key) {
		return call(jedis -> {
			return jedis.scard(key);
		});
	}

	public Long zadd(String key, double score, String member, ZAddParams params) {
		return call(jedis -> {
			return jedis.zadd(key, score, member, params);
		});
	}

	public Boolean sismember(byte[] key, byte[] member) {
		return call(jedis -> {
			return jedis.sismember(key, member);
		});
	}

	public Long zadd(String key, Map<String, Double> scoreMembers) {
		return call(jedis -> {
			return jedis.zadd(key, scoreMembers);
		});
	}

	public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
		return call(jedis -> {
			return jedis.zadd(key, scoreMembers, params);
		});
	}

	public Set<String> zrange(String key, long start, long end) {
		return call(jedis -> {
			return jedis.zrange(key, start, end);
		});
	}

	public Set<byte[]> sinter(byte[]... keys) {
		return call(jedis -> {
			return jedis.sinter(keys);
		});
	}

	public Long zrem(String key, String... members) {
		return call(jedis -> {
			return jedis.zrem(key, members);
		});
	}

	public Double zincrby(String key, double score, String member) {
		return call(jedis -> {
			return jedis.zincrby(key, score, member);
		});
	}

	public Long sinterstore(byte[] dstkey, byte[]... keys) {
		return call(jedis -> {
			return jedis.sinterstore(dstkey, keys);
		});
	}

	public Set<byte[]> sunion(byte[]... keys) {
		return call(jedis -> {
			return jedis.sunion(keys);
		});
	}

	public Double zincrby(String key, double score, String member, ZIncrByParams params) {
		return call(jedis -> {
			return jedis.zincrby(key, score, member, params);
		});
	}

	public Long sunionstore(byte[] dstkey, byte[]... keys) {
		return call(jedis -> {
			return jedis.sunionstore(dstkey, keys);
		});
	}

	public Long zrank(String key, String member) {
		return call(jedis -> {
			return jedis.zrank(key, member);
		});
	}

	public Set<byte[]> sdiff(byte[]... keys) {
		return call(jedis -> {
			return jedis.sdiff(keys);
		});
	}

	public Long zrevrank(String key, String member) {
		return call(jedis -> {
			return jedis.zrevrank(key, member);
		});
	}

	public Long sdiffstore(byte[] dstkey, byte[]... keys) {
		return call(jedis -> {
			return jedis.sdiffstore(dstkey, keys);
		});
	}

	public Set<String> zrevrange(String key, long start, long end) {
		return call(jedis -> {
			return jedis.zrevrange(key, start, end);
		});
	}

	public byte[] srandmember(byte[] key) {
		return call(jedis -> {
			return jedis.srandmember(key);
		});
	}

	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		return call(jedis -> {
			return jedis.zrangeWithScores(key, start, end);
		});
	}

	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		return call(jedis -> {
			return jedis.zrevrangeWithScores(key, start, end);
		});
	}

	public List<byte[]> srandmember(byte[] key, int count) {
		return call(jedis -> {
			return jedis.srandmember(key, count);
		});
	}

	public Long zcard(String key) {
		return call(jedis -> {
			return jedis.zcard(key);
		});
	}

	public Long zadd(byte[] key, double score, byte[] member) {
		return call(jedis -> {
			return jedis.zadd(key, score, member);
		});
	}

	public Double zscore(String key, String member) {
		return call(jedis -> {
			return jedis.zscore(key, member);
		});
	}

	public String watch(String... keys) {
		return call(jedis -> {
			return jedis.watch(keys);
		});
	}

	public List<String> sort(String key) {
		return call(jedis -> {
			return jedis.sort(key);
		});
	}

	public Long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
		return call(jedis -> {
			return jedis.zadd(key, score, member, params);
		});
	}

	public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
		return call(jedis -> {
			return jedis.zadd(key, scoreMembers);
		});
	}

	public Long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
		return call(jedis -> {
			return jedis.zadd(key, scoreMembers, params);
		});
	}

	public Set<byte[]> zrange(byte[] key, long start, long end) {
		return call(jedis -> {
			return jedis.zrange(key, start, end);
		});
	}

	public List<String> sort(String key, SortingParams sortingParameters) {
		return call(jedis -> {
			return jedis.sort(key, sortingParameters);
		});
	}

	public Long zrem(byte[] key, byte[]... members) {
		return call(jedis -> {
			return jedis.zrem(key, members);
		});
	}

	public Double zincrby(byte[] key, double score, byte[] member) {
		return call(jedis -> {
			return jedis.zincrby(key, score, member);
		});
	}

	public List<String> blpop(int timeout, String... keys) {
		return call(jedis -> {
			return jedis.blpop(timeout, keys);
		});
	}

	public Double zincrby(byte[] key, double score, byte[] member, ZIncrByParams params) {
		return call(jedis -> {
			return jedis.zincrby(key, score, member, params);
		});
	}

	public Long zrank(byte[] key, byte[] member) {
		return call(jedis -> {
			return jedis.zrank(key, member);
		});
	}

	public Long zrevrank(byte[] key, byte[] member) {
		return call(jedis -> {
			return jedis.zrevrank(key, member);
		});
	}

	public Set<byte[]> zrevrange(byte[] key, long start, long end) {
		return call(jedis -> {
			return jedis.zrevrange(key, start, end);
		});
	}

	public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
		return call(jedis -> {
			return jedis.zrangeWithScores(key, start, end);
		});
	}

	public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long end) {
		return call(jedis -> {
			return jedis.zrevrangeWithScores(key, start, end);
		});
	}

	public Long zcard(byte[] key) {
		return call(jedis -> {
			return jedis.zcard(key);
		});
	}

	public Double zscore(byte[] key, byte[] member) {
		return call(jedis -> {
			return jedis.zscore(key, member);
		});
	}

	public List<String> blpop(String... args) {
		return call(jedis -> {
			return jedis.blpop(args);
		});
	}

	public List<String> brpop(String... args) {
		return call(jedis -> {
			return jedis.brpop(args);
		});
	}

	public Transaction multi() {
		return call(jedis -> {
			return jedis.multi();
		});
	}

	public List<Object> multi(TransactionBlock jedisTransaction) {
		return call(jedis -> {
			return jedis.multi(jedisTransaction);
		});
	}

	public List<String> blpop(String arg) {
		return call(jedis -> {
			return jedis.blpop(arg);
		});
	}

	public List<String> brpop(String arg) {
		return call(jedis -> {
			return jedis.brpop(arg);
		});
	}

	public Long sort(String key, SortingParams sortingParameters, String dstkey) {
		return call(jedis -> {
			return jedis.sort(key, sortingParameters, dstkey);
		});
	}

	public Long sort(String key, String dstkey) {
		return call(jedis -> {
			return jedis.sort(key, dstkey);
		});
	}

	public String watch(byte[]... keys) {
		return call(jedis -> {
			return jedis.watch(keys);
		});
	}

	public String unwatch() {
		return call(jedis -> {
			return jedis.unwatch();
		});
	}

	public List<byte[]> sort(byte[] key) {
		return call(jedis -> {
			return jedis.sort(key);
		});
	}

	public List<String> brpop(int timeout, String... keys) {
		return call(jedis -> {
			return jedis.brpop(timeout, keys);
		});
	}

	public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
		return call(jedis -> {
			return jedis.sort(key, sortingParameters);
		});
	}

	public List<byte[]> blpop(int timeout, byte[]... keys) {
		return call(jedis -> {
			return jedis.blpop(timeout, keys);
		});
	}

	public Long zcount(String key, double min, double max) {
		return call(jedis -> {
			return jedis.zcount(key, min, max);
		});
	}

	public Long zcount(String key, String min, String max) {
		return call(jedis -> {
			return jedis.zcount(key, min, max);
		});
	}

	public Set<String> zrangeByScore(String key, double min, double max) {
		return call(jedis -> {
			return jedis.zrangeByScore(key, min, max);
		});
	}

	public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
		return call(jedis -> {
			return jedis.sort(key, sortingParameters, dstkey);
		});
	}

	public Long sort(byte[] key, byte[] dstkey) {
		return call(jedis -> {
			return jedis.sort(key, dstkey);
		});
	}

	public Set<String> zrangeByScore(String key, String min, String max) {
		return call(jedis -> {
			return jedis.zrangeByScore(key, min, max);
		});
	}

	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		return call(jedis -> {
			return jedis.zrangeByScore(key, min, max, offset, count);
		});
	}

	public List<byte[]> brpop(int timeout, byte[]... keys) {
		return call(jedis -> {
			return jedis.brpop(timeout, keys);
		});
	}

	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		return call(jedis -> {
			return jedis.zrangeByScore(key, min, max, offset, count);
		});
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		return call(jedis -> {
			return jedis.zrangeByScoreWithScores(key, min, max);
		});
	}

	public List<byte[]> blpop(byte[] arg) {
		return call(jedis -> {
			return jedis.blpop(arg);
		});
	}

	public List<byte[]> brpop(byte[] arg) {
		return call(jedis -> {
			return jedis.brpop(arg);
		});
	}

	public List<byte[]> blpop(byte[]... args) {
		return call(jedis -> {
			return jedis.blpop(args);
		});
	}

	public List<byte[]> brpop(byte[]... args) {
		return call(jedis -> {
			return jedis.brpop(args);
		});
	}

	public String auth(String password) {
		return call(jedis -> {
			return jedis.auth(password);
		});
	}

	public List<Object> pipelined(PipelineBlock jedisPipeline) {
		return call(jedis -> {
			return jedis.pipelined(jedisPipeline);
		});
	}

	public Pipeline pipelined() {
		return call(jedis -> {
			return jedis.pipelined();
		});
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		return call(jedis -> {
			return jedis.zrangeByScoreWithScores(key, min, max);
		});
	}

	public Long zcount(byte[] key, double min, double max) {
		return call(jedis -> {
			return jedis.zcount(key, min, max);
		});
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		return call(jedis -> {
			return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
		});
	}

	public Long zcount(byte[] key, byte[] min, byte[] max) {
		return call(jedis -> {
			return jedis.zcount(key, min, max);
		});
	}

	public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
		return call(jedis -> {
			return jedis.zrangeByScore(key, min, max);
		});
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		return call(jedis -> {
			return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
		});
	}

	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
		return call(jedis -> {
			return jedis.zrangeByScore(key, min, max);
		});
	}

	public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
		return call(jedis -> {
			return jedis.zrangeByScore(key, min, max, offset, count);
		});
	}

	public Set<String> zrevrangeByScore(String key, double max, double min) {
		return call(jedis -> {
			return jedis.zrevrangeByScore(key, max, min);
		});
	}

	public Set<String> zrevrangeByScore(String key, String max, String min) {
		return call(jedis -> {
			return jedis.zrevrangeByScore(key, max, min);
		});
	}

	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
		return call(jedis -> {
			return jedis.zrevrangeByScore(key, max, min, offset, count);
		});
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
		return call(jedis -> {
			return jedis.zrevrangeByScoreWithScores(key, max, min);
		});
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
		return call(jedis -> {
			return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
		});
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
		return call(jedis -> {
			return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
		});
	}

	public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
		return call(jedis -> {
			return jedis.zrevrangeByScore(key, max, min, offset, count);
		});
	}

	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return call(jedis -> {
			return jedis.zrangeByScore(key, min, max, offset, count);
		});
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
		return call(jedis -> {
			return jedis.zrevrangeByScoreWithScores(key, max, min);
		});
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
		return call(jedis -> {
			return jedis.zrangeByScoreWithScores(key, min, max);
		});
	}

	public Long zremrangeByRank(String key, long start, long end) {
		return call(jedis -> {
			return jedis.zremrangeByRank(key, start, end);
		});
	}

	public Long zremrangeByScore(String key, double start, double end) {
		return call(jedis -> {
			return jedis.zremrangeByScore(key, start, end);
		});
	}

	public Long zremrangeByScore(String key, String start, String end) {
		return call(jedis -> {
			return jedis.zremrangeByScore(key, start, end);
		});
	}

	public Long zunionstore(String dstkey, String... sets) {
		return call(jedis -> {
			return jedis.zunionstore(dstkey, sets);
		});
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
		return call(jedis -> {
			return jedis.zrangeByScoreWithScores(key, min, max);
		});
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
		return call(jedis -> {
			return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
		});
	}

	public Long zunionstore(String dstkey, ZParams params, String... sets) {
		return call(jedis -> {
			return jedis.zunionstore(dstkey, params, sets);
		});
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return call(jedis -> {
			return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
		});
	}

	public Long zinterstore(String dstkey, String... sets) {
		return call(jedis -> {
			return jedis.zinterstore(dstkey, sets);
		});
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
		return call(jedis -> {
			return jedis.zrevrangeByScore(key, max, min);
		});
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
		return call(jedis -> {
			return jedis.zrevrangeByScore(key, max, min);
		});
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
		return call(jedis -> {
			return jedis.zrevrangeByScore(key, max, min, offset, count);
		});
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return call(jedis -> {
			return jedis.zrevrangeByScore(key, max, min, offset, count);
		});
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
		return call(jedis -> {
			return jedis.zrevrangeByScoreWithScores(key, max, min);
		});
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
		return call(jedis -> {
			return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
		});
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
		return call(jedis -> {
			return jedis.zrevrangeByScoreWithScores(key, max, min);
		});
	}

	public Long zinterstore(String dstkey, ZParams params, String... sets) {
		return call(jedis -> {
			return jedis.zinterstore(dstkey, params, sets);
		});
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return call(jedis -> {
			return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
		});
	}

	public Long zremrangeByRank(byte[] key, long start, long end) {
		return call(jedis -> {
			return jedis.zremrangeByRank(key, start, end);
		});
	}

	public Long zremrangeByScore(byte[] key, double start, double end) {
		return call(jedis -> {
			return jedis.zremrangeByScore(key, start, end);
		});
	}

	public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
		return call(jedis -> {
			return jedis.zremrangeByScore(key, start, end);
		});
	}

	public Long zlexcount(String key, String min, String max) {
		return call(jedis -> {
			return jedis.zlexcount(key, min, max);
		});
	}

	public Long zunionstore(byte[] dstkey, byte[]... sets) {
		return call(jedis -> {
			return jedis.zunionstore(dstkey, sets);
		});
	}

	public Set<String> zrangeByLex(String key, String min, String max) {
		return call(jedis -> {
			return jedis.zrangeByLex(key, min, max);
		});
	}

	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
		return call(jedis -> {
			return jedis.zrangeByLex(key, min, max, offset, count);
		});
	}

	public Set<String> zrevrangeByLex(String key, String max, String min) {
		return call(jedis -> {
			return jedis.zrevrangeByLex(key, max, min);
		});
	}

	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		return call(jedis -> {
			return jedis.zrevrangeByLex(key, max, min, offset, count);
		});
	}

	public Long zremrangeByLex(String key, String min, String max) {
		return call(jedis -> {
			return jedis.zremrangeByLex(key, min, max);
		});
	}

	public Long strlen(String key) {
		return call(jedis -> {
			return jedis.strlen(key);
		});
	}

	public Long lpushx(String key, String... string) {
		return call(jedis -> {
			return jedis.lpushx(key, string);
		});
	}

	public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
		return call(jedis -> {
			return jedis.zunionstore(dstkey, params, sets);
		});
	}

	public Long persist(String key) {
		return call(jedis -> {
			return jedis.persist(key);
		});
	}

	public Long rpushx(String key, String... string) {
		return call(jedis -> {
			return jedis.rpushx(key, string);
		});
	}

	public String echo(String string) {
		return call(jedis -> {
			return jedis.echo(string);
		});
	}

	public Long linsert(String key, LIST_POSITION where, String pivot, String value) {
		return call(jedis -> {
			return jedis.linsert(key, where, pivot, value);
		});
	}

	public String brpoplpush(String source, String destination, int timeout) {
		return call(jedis -> {
			return jedis.brpoplpush(source, destination, timeout);
		});
	}

	public Boolean setbit(String key, long offset, boolean value) {
		return call(jedis -> {
			return jedis.setbit(key, offset, value);
		});
	}

	public Boolean setbit(String key, long offset, String value) {
		return call(jedis -> {
			return jedis.setbit(key, offset, value);
		});
	}

	public Boolean getbit(String key, long offset) {
		return call(jedis -> {
			return jedis.getbit(key, offset);
		});
	}

	public Long zinterstore(byte[] dstkey, byte[]... sets) {
		return call(jedis -> {
			return jedis.zinterstore(dstkey, sets);
		});
	}

	public Long setrange(String key, long offset, String value) {
		return call(jedis -> {
			return jedis.setrange(key, offset, value);
		});
	}

	public String getrange(String key, long startOffset, long endOffset) {
		return call(jedis -> {
			return jedis.getrange(key, startOffset, endOffset);
		});
	}

	public Long bitpos(String key, boolean value) {
		return call(jedis -> {
			return jedis.bitpos(key, value);
		});
	}

	public Long bitpos(String key, boolean value, BitPosParams params) {
		return call(jedis -> {
			return jedis.bitpos(key, value, params);
		});
	}

	public List<String> configGet(String pattern) {
		return call(jedis -> {
			return jedis.configGet(pattern);
		});
	}

	public String configSet(String parameter, String value) {
		return call(jedis -> {
			return jedis.configSet(parameter, value);
		});
	}

	public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
		return call(jedis -> {
			return jedis.zinterstore(dstkey, params, sets);
		});
	}

	public Object eval(String script, int keyCount, String... params) {
		return call(jedis -> {
			return jedis.eval(script, keyCount, params);
		});
	}

	public void subscribe(JedisPubSub jedisPubSub, String... channels) {
		execute(jedis -> {
			jedis.subscribe(jedisPubSub, channels);
		});
	}

	public Long publish(String channel, String message) {
		return call(jedis -> {
			return jedis.publish(channel, message);
		});
	}

	public Long zlexcount(byte[] key, byte[] min, byte[] max) {
		return call(jedis -> {
			return jedis.zlexcount(key, min, max);
		});
	}

	public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
		execute(jedis -> {
			jedis.psubscribe(jedisPubSub, patterns);
		});
	}

	public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
		return call(jedis -> {
			return jedis.zrangeByLex(key, min, max);
		});
	}

	public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return call(jedis -> {
			return jedis.zrangeByLex(key, min, max, offset, count);
		});
	}

	public Object eval(String script, List<String> keys, List<String> args) {
		return call(jedis -> {
			return jedis.eval(script, keys, args);
		});
	}

	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
		return call(jedis -> {
			return jedis.zrevrangeByLex(key, max, min);
		});
	}

	public Object eval(String script) {
		return call(jedis -> {
			return jedis.eval(script);
		});
	}

	public Object evalsha(String script) {
		return call(jedis -> {
			return jedis.evalsha(script);
		});
	}

	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return call(jedis -> {
			return jedis.zrevrangeByLex(key, max, min, offset, count);
		});
	}

	public Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
		return call(jedis -> {
			return jedis.zremrangeByLex(key, min, max);
		});
	}

	public String save() {
		return call(jedis -> {
			return jedis.save();
		});
	}

	public Object evalsha(String sha1, List<String> keys, List<String> args) {
		return call(jedis -> {
			return jedis.evalsha(sha1, keys, args);
		});
	}

	public Object evalsha(String sha1, int keyCount, String... params) {
		return call(jedis -> {
			return jedis.evalsha(sha1, keyCount, params);
		});
	}

	public Boolean scriptExists(String sha1) {
		return call(jedis -> {
			return jedis.scriptExists(sha1);
		});
	}

	public List<Boolean> scriptExists(String... sha1) {
		return call(jedis -> {
			return jedis.scriptExists(sha1);
		});
	}

	public String bgsave() {
		return call(jedis -> {
			return jedis.bgsave();
		});
	}

	public String scriptLoad(String script) {
		return call(jedis -> {
			return jedis.scriptLoad(script);
		});
	}

	public List<Slowlog> slowlogGet() {
		return call(jedis -> {
			return jedis.slowlogGet();
		});
	}

	public List<Slowlog> slowlogGet(long entries) {
		return call(jedis -> {
			return jedis.slowlogGet(entries);
		});
	}

	public String bgrewriteaof() {
		return call(jedis -> {
			return jedis.bgrewriteaof();
		});
	}

	public Long objectRefcount(String string) {
		return call(jedis -> {
			return jedis.objectRefcount(string);
		});
	}

	public String objectEncoding(String string) {
		return call(jedis -> {
			return jedis.objectEncoding(string);
		});
	}

	public Long objectIdletime(String string) {
		return call(jedis -> {
			return jedis.objectIdletime(string);
		});
	}

	public Long bitcount(String key) {
		return call(jedis -> {
			return jedis.bitcount(key);
		});
	}

	public Long bitcount(String key, long start, long end) {
		return call(jedis -> {
			return jedis.bitcount(key, start, end);
		});
	}

	public Long bitop(BitOP op, String destKey, String... srcKeys) {
		return call(jedis -> {
			return jedis.bitop(op, destKey, srcKeys);
		});
	}

	public List<Map<String, String>> sentinelMasters() {
		return call(jedis -> {
			return jedis.sentinelMasters();
		});
	}

	public Long lastsave() {
		return call(jedis -> {
			return jedis.lastsave();
		});
	}

	public List<String> sentinelGetMasterAddrByName(String masterName) {
		return call(jedis -> {
			return jedis.sentinelGetMasterAddrByName(masterName);
		});
	}

	public String info() {
		return call(jedis -> {
			return jedis.info();
		});
	}

	public Long sentinelReset(String pattern) {
		return call(jedis -> {
			return jedis.sentinelReset(pattern);
		});
	}

	public List<Map<String, String>> sentinelSlaves(String masterName) {
		return call(jedis -> {
			return jedis.sentinelSlaves(masterName);
		});
	}

	public String info(String section) {
		return call(jedis -> {
			return jedis.info(section);
		});
	}

	public void monitor(JedisMonitor jedisMonitor) {
		execute(jedis -> {
			jedis.monitor(jedisMonitor);
		});
	}

	public String sentinelFailover(String masterName) {
		return call(jedis -> {
			return jedis.sentinelFailover(masterName);
		});
	}

	public String sentinelMonitor(String masterName, String ip, int port, int quorum) {
		return call(jedis -> {
			return jedis.sentinelMonitor(masterName, ip, port, quorum);
		});
	}

	public String slaveof(String host, int port) {
		return call(jedis -> {
			return jedis.slaveof(host, port);
		});
	}

	public String sentinelRemove(String masterName) {
		return call(jedis -> {
			return jedis.sentinelRemove(masterName);
		});
	}

	public String sentinelSet(String masterName, Map<String, String> parameterMap) {
		return call(jedis -> {
			return jedis.sentinelSet(masterName, parameterMap);
		});
	}

	public byte[] dump(String key) {
		return call(jedis -> {
			return jedis.dump(key);
		});
	}

	public String restore(String key, int ttl, byte[] serializedValue) {
		return call(jedis -> {
			return jedis.restore(key, ttl, serializedValue);
		});
	}

	public Long pexpire(String key, int milliseconds) {
		return call(jedis -> {
			return jedis.pexpire(key, milliseconds);
		});
	}

	public String slaveofNoOne() {
		return call(jedis -> {
			return jedis.slaveofNoOne();
		});
	}

	public Long pexpire(String key, long milliseconds) {
		return call(jedis -> {
			return jedis.pexpire(key, milliseconds);
		});
	}

	public List<byte[]> configGet(byte[] pattern) {
		return call(jedis -> {
			return jedis.configGet(pattern);
		});
	}

	public Long pexpireAt(String key, long millisecondsTimestamp) {
		return call(jedis -> {
			return jedis.pexpireAt(key, millisecondsTimestamp);
		});
	}

	public Long pttl(String key) {
		return call(jedis -> {
			return jedis.pttl(key);
		});
	}

	public String psetex(String key, int milliseconds, String value) {
		return call(jedis -> {
			return jedis.psetex(key, milliseconds, value);
		});
	}

	public String psetex(String key, long milliseconds, String value) {
		return call(jedis -> {
			return jedis.psetex(key, milliseconds, value);
		});
	}

	public String configResetStat() {
		return call(jedis -> {
			return jedis.configResetStat();
		});
	}

	public String set(String key, String value, String nxxx) {
		return call(jedis -> {
			return jedis.set(key, value, nxxx);
		});
	}

	public byte[] configSet(byte[] parameter, byte[] value) {
		return call(jedis -> {
			return jedis.configSet(parameter, value);
		});
	}

	public String set(String key, String value, String nxxx, String expx, int time) {
		return call(jedis -> {
			return jedis.set(key, value, nxxx, expx, time);
		});
	}

	public String clientKill(String client) {
		return call(jedis -> {
			return jedis.clientKill(client);
		});
	}

	public String clientSetname(String name) {
		return call(jedis -> {
			return jedis.clientSetname(name);
		});
	}

	public String migrate(String host, int port, String key, int destinationDb, int timeout) {
		return call(jedis -> {
			return jedis.migrate(host, port, key, destinationDb, timeout);
		});
	}

	public ScanResult<String> scan(int cursor) {
		return call(jedis -> {
			return jedis.scan(cursor);
		});
	}

	public ScanResult<String> scan(int cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.scan(cursor, params);
		});
	}

	public Long strlen(byte[] key) {
		return call(jedis -> {
			return jedis.strlen(key);
		});
	}

	public void sync() {
		execute(jedis -> {
			jedis.sync();
		});
	}

	public Long lpushx(byte[] key, byte[]... string) {
		return call(jedis -> {
			return jedis.lpushx(key, string);
		});
	}

	public Long persist(byte[] key) {
		return call(jedis -> {
			return jedis.persist(key);
		});
	}

	public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
		return call(jedis -> {
			return jedis.hscan(key, cursor);
		});
	}

	public Long rpushx(byte[] key, byte[]... string) {
		return call(jedis -> {
			return jedis.rpushx(key, string);
		});
	}

	public ScanResult<Entry<String, String>> hscan(String key, int cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.hscan(key, cursor, params);
		});
	}

	public byte[] echo(byte[] string) {
		return call(jedis -> {
			return jedis.echo(string);
		});
	}

	public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot, byte[] value) {
		return call(jedis -> {
			return jedis.linsert(key, where, pivot, value);
		});
	}

	public String debug(DebugParams params) {
		return call(jedis -> {
			return jedis.debug(params);
		});
	}

	public Client getClient() {
		return call(jedis -> {
			return jedis.getClient();
		});
	}

	public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
		return call(jedis -> {
			return jedis.brpoplpush(source, destination, timeout);
		});
	}

	public ScanResult<String> sscan(String key, int cursor) {
		return call(jedis -> {
			return jedis.sscan(key, cursor);
		});
	}

	public Boolean setbit(byte[] key, long offset, boolean value) {
		return call(jedis -> {
			return jedis.setbit(key, offset, value);
		});
	}

	public ScanResult<String> sscan(String key, int cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.sscan(key, cursor, params);
		});
	}

	public Boolean setbit(byte[] key, long offset, byte[] value) {
		return call(jedis -> {
			return jedis.setbit(key, offset, value);
		});
	}

	public Boolean getbit(byte[] key, long offset) {
		return call(jedis -> {
			return jedis.getbit(key, offset);
		});
	}

	public Long bitpos(byte[] key, boolean value) {
		return call(jedis -> {
			return jedis.bitpos(key, value);
		});
	}

	public Long bitpos(byte[] key, boolean value, BitPosParams params) {
		return call(jedis -> {
			return jedis.bitpos(key, value, params);
		});
	}

	public Long setrange(byte[] key, long offset, byte[] value) {
		return call(jedis -> {
			return jedis.setrange(key, offset, value);
		});
	}

	public ScanResult<Tuple> zscan(String key, int cursor) {
		return call(jedis -> {
			return jedis.zscan(key, cursor);
		});
	}

	public byte[] getrange(byte[] key, long startOffset, long endOffset) {
		return call(jedis -> {
			return jedis.getrange(key, startOffset, endOffset);
		});
	}

	public Long publish(byte[] channel, byte[] message) {
		return call(jedis -> {
			return jedis.publish(channel, message);
		});
	}

	public ScanResult<Tuple> zscan(String key, int cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.zscan(key, cursor, params);
		});
	}

	public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
		execute(jedis -> {
			jedis.subscribe(jedisPubSub, channels);
		});
	}

	public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
		execute(jedis -> {
			jedis.psubscribe(jedisPubSub, patterns);
		});
	}

	public Long getDB() {
		return call(jedis -> {
			return jedis.getDB();
		});
	}

	public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
		return call(jedis -> {
			return jedis.eval(script, keys, args);
		});
	}

	public ScanResult<String> scan(String cursor) {
		return call(jedis -> {
			return jedis.scan(cursor);
		});
	}

	public ScanResult<String> scan(String cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.scan(cursor, params);
		});
	}

	public Object eval(byte[] script, byte[] keyCount, byte[]... params) {
		return call(jedis -> {
			return jedis.eval(script, keyCount, params);
		});
	}

	public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
		return call(jedis -> {
			return jedis.hscan(key, cursor);
		});
	}

	public Object eval(byte[] script, int keyCount, byte[]... params) {
		return call(jedis -> {
			return jedis.eval(script, keyCount, params);
		});
	}

	public Object eval(byte[] script, byte[] key) {
		return call(jedis -> {
			return jedis.eval(script, key);
		});
	}

	public byte[] scriptLoad(byte[] script, byte[] key) {
		throw new UnsupportedOperationException("Method unsupported in single redis client");
	}

	public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.hscan(key, cursor, params);
		});
	}

	public Object eval(byte[] script) {
		return call(jedis -> {
			return jedis.eval(script);
		});
	}

	public Object evalsha(byte[] sha1) {
		return call(jedis -> {
			return jedis.evalsha(sha1);
		});
	}

	public Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
		return call(jedis -> {
			return jedis.evalsha(sha1, keys, args);
		});
	}

	public Object evalsha(byte[] sha1, int keyCount, byte[]... params) {
		return call(jedis -> {
			return jedis.evalsha(sha1, keyCount, params);
		});
	}

	public String scriptFlush() {
		return call(jedis -> {
			return jedis.scriptFlush();
		});
	}

	public Long scriptExists(byte[] sha1) {
		return call(jedis -> {
			return jedis.scriptExists(sha1);
		});
	}

	public List<Long> scriptExists(byte[]... sha1) {
		return call(jedis -> {
			return jedis.scriptExists(sha1);
		});
	}

	public ScanResult<String> sscan(String key, String cursor) {
		return call(jedis -> {
			return jedis.sscan(key, cursor);
		});
	}

	public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.sscan(key, cursor, params);
		});
	}

	public byte[] scriptLoad(byte[] script) {
		return call(jedis -> {
			return jedis.scriptLoad(script);
		});
	}

	public String scriptKill() {
		return call(jedis -> {
			return jedis.scriptKill();
		});
	}

	public String slowlogReset() {
		return call(jedis -> {
			return jedis.slowlogReset();
		});
	}

	public Long slowlogLen() {
		return call(jedis -> {
			return jedis.slowlogLen();
		});
	}

	public List<byte[]> slowlogGetBinary() {
		return call(jedis -> {
			return jedis.slowlogGetBinary();
		});
	}

	public List<byte[]> slowlogGetBinary(long entries) {
		return call(jedis -> {
			return jedis.slowlogGetBinary(entries);
		});
	}

	public ScanResult<Tuple> zscan(String key, String cursor) {
		return call(jedis -> {
			return jedis.zscan(key, cursor);
		});
	}

	public Long objectRefcount(byte[] key) {
		return call(jedis -> {
			return jedis.objectRefcount(key);
		});
	}

	public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.zscan(key, cursor, params);
		});
	}

	public byte[] objectEncoding(byte[] key) {
		return call(jedis -> {
			return jedis.objectEncoding(key);
		});
	}

	public Long objectIdletime(byte[] key) {
		return call(jedis -> {
			return jedis.objectIdletime(key);
		});
	}

	public Long bitcount(byte[] key) {
		return call(jedis -> {
			return jedis.bitcount(key);
		});
	}

	public Long bitcount(byte[] key, long start, long end) {
		return call(jedis -> {
			return jedis.bitcount(key, start, end);
		});
	}

	public Long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
		return call(jedis -> {
			return jedis.bitop(op, destKey, srcKeys);
		});
	}

	public String clusterNodes() {
		return call(jedis -> {
			return jedis.clusterNodes();
		});
	}

	public byte[] dump(byte[] key) {
		return call(jedis -> {
			return jedis.dump(key);
		});
	}

	public String readonly() {
		return call(jedis -> {
			return jedis.readonly();
		});
	}

	public String restore(byte[] key, int ttl, byte[] serializedValue) {
		return call(jedis -> {
			return jedis.restore(key, ttl, serializedValue);
		});
	}

	public String clusterMeet(String ip, int port) {
		return call(jedis -> {
			return jedis.clusterMeet(ip, port);
		});
	}

	public String clusterReset(Reset resetType) {
		return call(jedis -> {
			return jedis.clusterReset(resetType);
		});
	}

	public Long pexpire(byte[] key, int milliseconds) {
		return call(jedis -> {
			return jedis.pexpire(key, milliseconds);
		});
	}

	public Long pexpire(byte[] key, long milliseconds) {
		return call(jedis -> {
			return jedis.pexpire(key, milliseconds);
		});
	}

	public String clusterAddSlots(int... slots) {
		return call(jedis -> {
			return jedis.clusterAddSlots(slots);
		});
	}

	public String clusterDelSlots(int... slots) {
		return call(jedis -> {
			return jedis.clusterDelSlots(slots);
		});
	}

	public Long pexpireAt(byte[] key, long millisecondsTimestamp) {
		return call(jedis -> {
			return jedis.pexpireAt(key, millisecondsTimestamp);
		});
	}

	public String clusterInfo() {
		return call(jedis -> {
			return jedis.clusterInfo();
		});
	}

	public Long pttl(byte[] key) {
		return call(jedis -> {
			return jedis.pttl(key);
		});
	}

	public List<String> clusterGetKeysInSlot(int slot, int count) {
		return call(jedis -> {
			return jedis.clusterGetKeysInSlot(slot, count);
		});
	}

	public String psetex(byte[] key, int milliseconds, byte[] value) {
		return call(jedis -> {
			return jedis.psetex(key, milliseconds, value);
		});
	}

	public String psetex(byte[] key, long milliseconds, byte[] value) {
		return call(jedis -> {
			return jedis.psetex(key, milliseconds, value);
		});
	}

	public String clusterSetSlotNode(int slot, String nodeId) {
		return call(jedis -> {
			return jedis.clusterSetSlotNode(slot, nodeId);
		});
	}

	public String clusterSetSlotMigrating(int slot, String nodeId) {
		return call(jedis -> {
			return jedis.clusterSetSlotMigrating(slot, nodeId);
		});
	}

	public String clusterSetSlotImporting(int slot, String nodeId) {
		return call(jedis -> {
			return jedis.clusterSetSlotImporting(slot, nodeId);
		});
	}

	public String set(byte[] key, byte[] value, byte[] nxxx) {
		return call(jedis -> {
			return jedis.set(key, value, nxxx);
		});
	}

	public String clusterSetSlotStable(int slot) {
		return call(jedis -> {
			return jedis.clusterSetSlotStable(slot);
		});
	}

	public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, int time) {
		return call(jedis -> {
			return jedis.set(key, value, nxxx, expx, time);
		});
	}

	public String clusterForget(String nodeId) {
		return call(jedis -> {
			return jedis.clusterForget(nodeId);
		});
	}

	public String clientKill(byte[] client) {
		return call(jedis -> {
			return jedis.clientKill(client);
		});
	}

	public String clusterFlushSlots() {
		return call(jedis -> {
			return jedis.clusterFlushSlots();
		});
	}

	public Long clusterKeySlot(String key) {
		return call(jedis -> {
			return jedis.clusterKeySlot(key);
		});
	}

	public String clientGetname() {
		return call(jedis -> {
			return jedis.clientGetname();
		});
	}

	public String clientList() {
		return call(jedis -> {
			return jedis.clientList();
		});
	}

	public Long clusterCountKeysInSlot(int slot) {
		return call(jedis -> {
			return jedis.clusterCountKeysInSlot(slot);
		});
	}

	public String clientSetname(byte[] name) {
		return call(jedis -> {
			return jedis.clientSetname(name);
		});
	}

	public String clusterSaveConfig() {
		return call(jedis -> {
			return jedis.clusterSaveConfig();
		});
	}

	public List<String> time() {
		return call(jedis -> {
			return jedis.time();
		});
	}

	public String clusterReplicate(String nodeId) {
		return call(jedis -> {
			return jedis.clusterReplicate(nodeId);
		});
	}

	public String migrate(byte[] host, int port, byte[] key, int destinationDb, int timeout) {
		return call(jedis -> {
			return jedis.migrate(host, port, key, destinationDb, timeout);
		});
	}

	public List<String> clusterSlaves(String nodeId) {
		return call(jedis -> {
			return jedis.clusterSlaves(nodeId);
		});
	}

	public String clusterFailover() {
		return call(jedis -> {
			return jedis.clusterFailover();
		});
	}

	public Long waitReplicas(int replicas, long timeout) {
		return call(jedis -> {
			return jedis.waitReplicas(replicas, timeout);
		});
	}

	public List<Object> clusterSlots() {
		return call(jedis -> {
			return jedis.clusterSlots();
		});
	}

	public String asking() {
		return call(jedis -> {
			return jedis.asking();
		});
	}

	public Long pfadd(byte[] key, byte[]... elements) {
		return call(jedis -> {
			return jedis.pfadd(key, elements);
		});
	}

	public List<String> pubsubChannels(String pattern) {
		return call(jedis -> {
			return jedis.pubsubChannels(pattern);
		});
	}

	public long pfcount(byte[] key) {
		return call(jedis -> {
			return jedis.pfcount(key);
		});
	}

	public Long pubsubNumPat() {
		return call(jedis -> {
			return jedis.pubsubNumPat();
		});
	}

	public Map<String, String> pubsubNumSub(String... channels) {
		return call(jedis -> {
			return jedis.pubsubNumSub(channels);
		});
	}

	public String pfmerge(byte[] destkey, byte[]... sourcekeys) {
		return call(jedis -> {
			return jedis.pfmerge(destkey, sourcekeys);
		});
	}

	public Long pfcount(byte[]... keys) {
		return call(jedis -> {
			return jedis.pfcount(keys);
		});
	}

	public ScanResult<byte[]> scan(byte[] cursor) {
		return call(jedis -> {
			return jedis.scan(cursor);
		});
	}

	public ScanResult<byte[]> scan(byte[] cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.scan(cursor, params);
		});
	}

	public Long pfadd(String key, String... elements) {
		return call(jedis -> {
			return jedis.pfadd(key, elements);
		});
	}

	public long pfcount(String key) {
		return call(jedis -> {
			return jedis.pfcount(key);
		});
	}

	public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor) {
		return call(jedis -> {
			return jedis.hscan(key, cursor);
		});
	}

	public long pfcount(String... keys) {
		return call(jedis -> {
			return jedis.pfcount(keys);
		});
	}

	public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.hscan(key, cursor, params);
		});
	}

	public String pfmerge(String destkey, String... sourcekeys) {
		return call(jedis -> {
			return jedis.pfmerge(destkey, sourcekeys);
		});
	}

	public List<String> blpop(int timeout, String key) {
		return call(jedis -> {
			return jedis.blpop(timeout, key);
		});
	}

	public List<String> brpop(int timeout, String key) {
		return call(jedis -> {
			return jedis.brpop(timeout, key);
		});
	}

	public Long geoadd(String key, double longitude, double latitude, String member) {
		return call(jedis -> {
			return jedis.geoadd(key, longitude, latitude, member);
		});
	}

	public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
		return call(jedis -> {
			return jedis.geoadd(key, memberCoordinateMap);
		});
	}

	public ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
		return call(jedis -> {
			return jedis.sscan(key, cursor);
		});
	}

	public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.sscan(key, cursor, params);
		});
	}

	public Double geodist(String key, String member1, String member2) {
		return call(jedis -> {
			return jedis.geodist(key, member1, member2);
		});
	}

	public Double geodist(String key, String member1, String member2, GeoUnit unit) {
		return call(jedis -> {
			return jedis.geodist(key, member1, member2, unit);
		});
	}

	public ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
		return call(jedis -> {
			return jedis.zscan(key, cursor);
		});
	}

	public List<String> geohash(String key, String... members) {
		return call(jedis -> {
			return jedis.geohash(key, members);
		});
	}

	public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
		return call(jedis -> {
			return jedis.zscan(key, cursor, params);
		});
	}

	public List<GeoCoordinate> geopos(String key, String... members) {
		return call(jedis -> {
			return jedis.geopos(key, members);
		});
	}

	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		return call(jedis -> {
			return jedis.georadius(key, longitude, latitude, radius, unit);
		});
	}

	public Long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
		return call(jedis -> {
			return jedis.geoadd(key, longitude, latitude, member);
		});
	}

	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		return call(jedis -> {
			return jedis.georadius(key, longitude, latitude, radius, unit, param);
		});
	}

	public Long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
		return call(jedis -> {
			return jedis.geoadd(key, memberCoordinateMap);
		});
	}

	public Double geodist(byte[] key, byte[] member1, byte[] member2) {
		return call(jedis -> {
			return jedis.geodist(key, member1, member2);
		});
	}

	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
		return call(jedis -> {
			return jedis.georadiusByMember(key, member, radius, unit);
		});
	}

	public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
		return call(jedis -> {
			return jedis.geodist(key, member1, member2, unit);
		});
	}

	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		return call(jedis -> {
			return jedis.georadiusByMember(key, member, radius, unit, param);
		});
	}

	public List<byte[]> geohash(byte[] key, byte[]... members) {
		return call(jedis -> {
			return jedis.geohash(key, members);
		});
	}

	public List<Long> bitfield(String key, String... arguments) {
		return call(jedis -> {
			return jedis.bitfield(key, arguments);
		});
	}

	public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
		return call(jedis -> {
			return jedis.geopos(key, members);
		});
	}

	public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		return call(jedis -> {
			return jedis.georadius(key, longitude, latitude, radius, unit);
		});
	}

	public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		return call(jedis -> {
			return jedis.georadius(key, longitude, latitude, radius, unit, param);
		});
	}

	public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
		return call(jedis -> {
			return jedis.georadiusByMember(key, member, radius, unit);
		});
	}

	public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		return call(jedis -> {
			return jedis.georadiusByMember(key, member, radius, unit, param);
		});
	}

	public List<byte[]> bitfield(byte[] key, byte[]... arguments) {
		return call(jedis -> {
			return jedis.bitfield(key, arguments);
		});
	}
}
