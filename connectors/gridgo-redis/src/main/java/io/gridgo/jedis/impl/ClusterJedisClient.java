package io.gridgo.jedis.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.gridgo.connector.redis.RedisConfig;
import io.gridgo.jedis.JedisClient;
import lombok.NonNull;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.Client;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCluster.Reset;
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
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.Slowlog;

@SuppressWarnings("deprecation")
public class ClusterJedisClient extends JedisClient {

	private final JedisCluster jedisCluster;

	public ClusterJedisClient(@NonNull RedisConfig config) {
		Set<HostAndPort> clusters = new HashSet<>();
		config.getAddress().forEach((address) -> {
			clusters.add(new HostAndPort(address.getHost(), address.getPort()));
		});
		this.jedisCluster = new JedisCluster(clusters);
	}

	@Override
	protected void onStop() {
		try {
			jedisCluster.close();
		} catch (IOException e) {
			throw new RuntimeException("Cannot close jedis cluster", e);
		}
	}

	public Map<String, JedisPool> getClusterNodes() {
		return jedisCluster.getClusterNodes();
	}

	public String set(byte[] key, byte[] value) {
		return jedisCluster.set(key, value);
	}

	public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time) {
		return jedisCluster.set(key, value, nxxx, expx, time);
	}

	public byte[] get(byte[] key) {
		return jedisCluster.get(key);
	}

	public Boolean exists(byte[] key) {
		return jedisCluster.exists(key);
	}

	public boolean equals(Object obj) {
		return jedisCluster.equals(obj);
	}

	public String set(String key, String value) {
		return jedisCluster.set(key, value);
	}

	public Long exists(byte[]... keys) {
		return jedisCluster.exists(keys);
	}

	public String set(String key, String value, String nxxx, String expx, long time) {
		return jedisCluster.set(key, value, nxxx, expx, time);
	}

	public Long persist(byte[] key) {
		return jedisCluster.persist(key);
	}

	public String type(byte[] key) {
		return jedisCluster.type(key);
	}

	public String get(String key) {
		return jedisCluster.get(key);
	}

	public Long expire(byte[] key, int seconds) {
		return jedisCluster.expire(key, seconds);
	}

	public Boolean exists(String key) {
		return jedisCluster.exists(key);
	}

	public Long pexpire(byte[] key, long milliseconds) {
		return jedisCluster.pexpire(key, milliseconds);
	}

	public Long exists(String... keys) {
		return jedisCluster.exists(keys);
	}

	public Long persist(String key) {
		return jedisCluster.persist(key);
	}

	public Long expireAt(byte[] key, long unixTime) {
		return jedisCluster.expireAt(key, unixTime);
	}

	public String type(String key) {
		return jedisCluster.type(key);
	}

	public Long pexpireAt(byte[] key, long millisecondsTimestamp) {
		return jedisCluster.pexpireAt(key, millisecondsTimestamp);
	}

	public Long expire(String key, int seconds) {
		return jedisCluster.expire(key, seconds);
	}

	public Long ttl(byte[] key) {
		return jedisCluster.ttl(key);
	}

	public Long pexpire(String key, long milliseconds) {
		return jedisCluster.pexpire(key, milliseconds);
	}

	public Boolean setbit(byte[] key, long offset, boolean value) {
		return jedisCluster.setbit(key, offset, value);
	}

	public Long expireAt(String key, long unixTime) {
		return jedisCluster.expireAt(key, unixTime);
	}

	public Boolean setbit(byte[] key, long offset, byte[] value) {
		return jedisCluster.setbit(key, offset, value);
	}

	public Long pexpireAt(String key, long millisecondsTimestamp) {
		return jedisCluster.pexpireAt(key, millisecondsTimestamp);
	}

	public Boolean getbit(byte[] key, long offset) {
		return jedisCluster.getbit(key, offset);
	}

	public Long ttl(String key) {
		return jedisCluster.ttl(key);
	}

	public Long setrange(byte[] key, long offset, byte[] value) {
		return jedisCluster.setrange(key, offset, value);
	}

	public Long pttl(String key) {
		return jedisCluster.pttl(key);
	}

	public byte[] getrange(byte[] key, long startOffset, long endOffset) {
		return jedisCluster.getrange(key, startOffset, endOffset);
	}

	public Boolean setbit(String key, long offset, boolean value) {
		return jedisCluster.setbit(key, offset, value);
	}

	public byte[] getSet(byte[] key, byte[] value) {
		return jedisCluster.getSet(key, value);
	}

	public Boolean setbit(String key, long offset, String value) {
		return jedisCluster.setbit(key, offset, value);
	}

	public Long setnx(byte[] key, byte[] value) {
		return jedisCluster.setnx(key, value);
	}

	public Boolean getbit(String key, long offset) {
		return jedisCluster.getbit(key, offset);
	}

	public String setex(byte[] key, int seconds, byte[] value) {
		return jedisCluster.setex(key, seconds, value);
	}

	public Long setrange(String key, long offset, String value) {
		return jedisCluster.setrange(key, offset, value);
	}

	public Long decrBy(byte[] key, long integer) {
		return jedisCluster.decrBy(key, integer);
	}

	public String getrange(String key, long startOffset, long endOffset) {
		return jedisCluster.getrange(key, startOffset, endOffset);
	}

	public Long decr(byte[] key) {
		return jedisCluster.decr(key);
	}

	public String getSet(String key, String value) {
		return jedisCluster.getSet(key, value);
	}

	public Long incrBy(byte[] key, long integer) {
		return jedisCluster.incrBy(key, integer);
	}

	public String toString() {
		return jedisCluster.toString();
	}

	public Long setnx(String key, String value) {
		return jedisCluster.setnx(key, value);
	}

	public Double incrByFloat(byte[] key, double value) {
		return jedisCluster.incrByFloat(key, value);
	}

	public String setex(String key, int seconds, String value) {
		return jedisCluster.setex(key, seconds, value);
	}

	public Long incr(byte[] key) {
		return jedisCluster.incr(key);
	}

	public String psetex(String key, long milliseconds, String value) {
		return jedisCluster.psetex(key, milliseconds, value);
	}

	public Long append(byte[] key, byte[] value) {
		return jedisCluster.append(key, value);
	}

	public Long decrBy(String key, long integer) {
		return jedisCluster.decrBy(key, integer);
	}

	public byte[] substr(byte[] key, int start, int end) {
		return jedisCluster.substr(key, start, end);
	}

	public Long decr(String key) {
		return jedisCluster.decr(key);
	}

	public Long hset(byte[] key, byte[] field, byte[] value) {
		return jedisCluster.hset(key, field, value);
	}

	public Long incrBy(String key, long integer) {
		return jedisCluster.incrBy(key, integer);
	}

	public byte[] hget(byte[] key, byte[] field) {
		return jedisCluster.hget(key, field);
	}

	public Double incrByFloat(String key, double value) {
		return jedisCluster.incrByFloat(key, value);
	}

	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		return jedisCluster.hsetnx(key, field, value);
	}

	public Long incr(String key) {
		return jedisCluster.incr(key);
	}

	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		return jedisCluster.hmset(key, hash);
	}

	public Long append(String key, String value) {
		return jedisCluster.append(key, value);
	}

	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		return jedisCluster.hmget(key, fields);
	}

	public String substr(String key, int start, int end) {
		return jedisCluster.substr(key, start, end);
	}

	public Long hincrBy(byte[] key, byte[] field, long value) {
		return jedisCluster.hincrBy(key, field, value);
	}

	public Long hset(String key, String field, String value) {
		return jedisCluster.hset(key, field, value);
	}

	public Double hincrByFloat(byte[] key, byte[] field, double value) {
		return jedisCluster.hincrByFloat(key, field, value);
	}

	public String hget(String key, String field) {
		return jedisCluster.hget(key, field);
	}

	public Long hsetnx(String key, String field, String value) {
		return jedisCluster.hsetnx(key, field, value);
	}

	public Boolean hexists(byte[] key, byte[] field) {
		return jedisCluster.hexists(key, field);
	}

	public String hmset(String key, Map<String, String> hash) {
		return jedisCluster.hmset(key, hash);
	}

	public Long hdel(byte[] key, byte[]... field) {
		return jedisCluster.hdel(key, field);
	}

	public List<String> hmget(String key, String... fields) {
		return jedisCluster.hmget(key, fields);
	}

	public Long hlen(byte[] key) {
		return jedisCluster.hlen(key);
	}

	public Set<byte[]> hkeys(byte[] key) {
		return jedisCluster.hkeys(key);
	}

	public Long hincrBy(String key, String field, long value) {
		return jedisCluster.hincrBy(key, field, value);
	}

	public List<byte[]> hvals(byte[] key) {
		return (List<byte[]>) jedisCluster.hvals(key);
	}

	public Double hincrByFloat(String key, String field, double value) {
		return jedisCluster.hincrByFloat(key, field, value);
	}

	public Map<byte[], byte[]> hgetAll(byte[] key) {
		return jedisCluster.hgetAll(key);
	}

	public Boolean hexists(String key, String field) {
		return jedisCluster.hexists(key, field);
	}

	public Long rpush(byte[] key, byte[]... args) {
		return jedisCluster.rpush(key, args);
	}

	public Long hdel(String key, String... field) {
		return jedisCluster.hdel(key, field);
	}

	public Long lpush(byte[] key, byte[]... args) {
		return jedisCluster.lpush(key, args);
	}

	public Long hlen(String key) {
		return jedisCluster.hlen(key);
	}

	public Long llen(byte[] key) {
		return jedisCluster.llen(key);
	}

	public Set<String> hkeys(String key) {
		return jedisCluster.hkeys(key);
	}

	public List<byte[]> lrange(byte[] key, long start, long end) {
		return jedisCluster.lrange(key, start, end);
	}

	public List<String> hvals(String key) {
		return jedisCluster.hvals(key);
	}

	public Map<String, String> hgetAll(String key) {
		return jedisCluster.hgetAll(key);
	}

	public String ltrim(byte[] key, long start, long end) {
		return jedisCluster.ltrim(key, start, end);
	}

	public Long rpush(String key, String... string) {
		return jedisCluster.rpush(key, string);
	}

	public byte[] lindex(byte[] key, long index) {
		return jedisCluster.lindex(key, index);
	}

	public Long lpush(String key, String... string) {
		return jedisCluster.lpush(key, string);
	}

	public String lset(byte[] key, long index, byte[] value) {
		return jedisCluster.lset(key, index, value);
	}

	public Long llen(String key) {
		return jedisCluster.llen(key);
	}

	public Long lrem(byte[] key, long count, byte[] value) {
		return jedisCluster.lrem(key, count, value);
	}

	public List<String> lrange(String key, long start, long end) {
		return jedisCluster.lrange(key, start, end);
	}

	public byte[] lpop(byte[] key) {
		return jedisCluster.lpop(key);
	}

	public String ltrim(String key, long start, long end) {
		return jedisCluster.ltrim(key, start, end);
	}

	public byte[] rpop(byte[] key) {
		return jedisCluster.rpop(key);
	}

	public String lindex(String key, long index) {
		return jedisCluster.lindex(key, index);
	}

	public Long sadd(byte[] key, byte[]... member) {
		return jedisCluster.sadd(key, member);
	}

	public String lset(String key, long index, String value) {
		return jedisCluster.lset(key, index, value);
	}

	public Set<byte[]> smembers(byte[] key) {
		return jedisCluster.smembers(key);
	}

	public Long srem(byte[] key, byte[]... member) {
		return jedisCluster.srem(key, member);
	}

	public Long lrem(String key, long count, String value) {
		return jedisCluster.lrem(key, count, value);
	}

	public byte[] spop(byte[] key) {
		return jedisCluster.spop(key);
	}

	public String lpop(String key) {
		return jedisCluster.lpop(key);
	}

	public Set<byte[]> spop(byte[] key, long count) {
		return jedisCluster.spop(key, count);
	}

	public String rpop(String key) {
		return jedisCluster.rpop(key);
	}

	public Long sadd(String key, String... member) {
		return jedisCluster.sadd(key, member);
	}

	public Long scard(byte[] key) {
		return jedisCluster.scard(key);
	}

	public Boolean sismember(byte[] key, byte[] member) {
		return jedisCluster.sismember(key, member);
	}

	public Set<String> smembers(String key) {
		return jedisCluster.smembers(key);
	}

	public Long srem(String key, String... member) {
		return jedisCluster.srem(key, member);
	}

	public byte[] srandmember(byte[] key) {
		return jedisCluster.srandmember(key);
	}

	public String spop(String key) {
		return jedisCluster.spop(key);
	}

	public Long strlen(byte[] key) {
		return jedisCluster.strlen(key);
	}

	public Set<String> spop(String key, long count) {
		return jedisCluster.spop(key, count);
	}

	public Long zadd(byte[] key, double score, byte[] member) {
		return jedisCluster.zadd(key, score, member);
	}

	public Long scard(String key) {
		return jedisCluster.scard(key);
	}

	public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
		return jedisCluster.zadd(key, scoreMembers);
	}

	public Boolean sismember(String key, String member) {
		return jedisCluster.sismember(key, member);
	}

	public Long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
		return jedisCluster.zadd(key, score, member, params);
	}

	public String srandmember(String key) {
		return jedisCluster.srandmember(key);
	}

	public Long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
		return jedisCluster.zadd(key, scoreMembers, params);
	}

	public List<String> srandmember(String key, int count) {
		return jedisCluster.srandmember(key, count);
	}

	public Set<byte[]> zrange(byte[] key, long start, long end) {
		return jedisCluster.zrange(key, start, end);
	}

	public Long strlen(String key) {
		return jedisCluster.strlen(key);
	}

	public Long zrem(byte[] key, byte[]... member) {
		return jedisCluster.zrem(key, member);
	}

	public Long zadd(String key, double score, String member) {
		return jedisCluster.zadd(key, score, member);
	}

	public Double zincrby(byte[] key, double score, byte[] member) {
		return jedisCluster.zincrby(key, score, member);
	}

	public Long zadd(String key, double score, String member, ZAddParams params) {
		return jedisCluster.zadd(key, score, member, params);
	}

	public Double zincrby(byte[] key, double score, byte[] member, ZIncrByParams params) {
		return jedisCluster.zincrby(key, score, member, params);
	}

	public Long zadd(String key, Map<String, Double> scoreMembers) {
		return jedisCluster.zadd(key, scoreMembers);
	}

	public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
		return jedisCluster.zadd(key, scoreMembers, params);
	}

	public Long zrank(byte[] key, byte[] member) {
		return jedisCluster.zrank(key, member);
	}

	public Long zrevrank(byte[] key, byte[] member) {
		return jedisCluster.zrevrank(key, member);
	}

	public Set<String> zrange(String key, long start, long end) {
		return jedisCluster.zrange(key, start, end);
	}

	public Set<byte[]> zrevrange(byte[] key, long start, long end) {
		return jedisCluster.zrevrange(key, start, end);
	}

	public Long zrem(String key, String... member) {
		return jedisCluster.zrem(key, member);
	}

	public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
		return jedisCluster.zrangeWithScores(key, start, end);
	}

	public Double zincrby(String key, double score, String member) {
		return jedisCluster.zincrby(key, score, member);
	}

	public Double zincrby(String key, double score, String member, ZIncrByParams params) {
		return jedisCluster.zincrby(key, score, member, params);
	}

	public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long end) {
		return jedisCluster.zrevrangeWithScores(key, start, end);
	}

	public Long zcard(byte[] key) {
		return jedisCluster.zcard(key);
	}

	public Long zrank(String key, String member) {
		return jedisCluster.zrank(key, member);
	}

	public Double zscore(byte[] key, byte[] member) {
		return jedisCluster.zscore(key, member);
	}

	public Long zrevrank(String key, String member) {
		return jedisCluster.zrevrank(key, member);
	}

	public List<byte[]> sort(byte[] key) {
		return jedisCluster.sort(key);
	}

	public Set<String> zrevrange(String key, long start, long end) {
		return jedisCluster.zrevrange(key, start, end);
	}

	public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
		return jedisCluster.sort(key, sortingParameters);
	}

	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		return jedisCluster.zrangeWithScores(key, start, end);
	}

	public Long zcount(byte[] key, double min, double max) {
		return jedisCluster.zcount(key, min, max);
	}

	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		return jedisCluster.zrevrangeWithScores(key, start, end);
	}

	public Long zcount(byte[] key, byte[] min, byte[] max) {
		return jedisCluster.zcount(key, min, max);
	}

	public Long zcard(String key) {
		return jedisCluster.zcard(key);
	}

	public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
		return jedisCluster.zrangeByScore(key, min, max);
	}

	public Double zscore(String key, String member) {
		return jedisCluster.zscore(key, member);
	}

	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
		return jedisCluster.zrangeByScore(key, min, max);
	}

	public List<String> sort(String key) {
		return jedisCluster.sort(key);
	}

	public List<String> sort(String key, SortingParams sortingParameters) {
		return jedisCluster.sort(key, sortingParameters);
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
		return jedisCluster.zrevrangeByScore(key, max, min);
	}

	public Long zcount(String key, double min, double max) {
		return jedisCluster.zcount(key, min, max);
	}

	public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
		return jedisCluster.zrangeByScore(key, min, max, offset, count);
	}

	public Long zcount(String key, String min, String max) {
		return jedisCluster.zcount(key, min, max);
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
		return jedisCluster.zrevrangeByScore(key, max, min);
	}

	public Set<String> zrangeByScore(String key, double min, double max) {
		return jedisCluster.zrangeByScore(key, min, max);
	}

	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return jedisCluster.zrangeByScore(key, min, max, offset, count);
	}

	public Set<String> zrangeByScore(String key, String min, String max) {
		return jedisCluster.zrangeByScore(key, min, max);
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
		return jedisCluster.zrevrangeByScore(key, max, min, offset, count);
	}

	public Set<String> zrevrangeByScore(String key, double max, double min) {
		return jedisCluster.zrevrangeByScore(key, max, min);
	}

	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		return jedisCluster.zrangeByScore(key, min, max, offset, count);
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
		return jedisCluster.zrangeByScoreWithScores(key, min, max);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
		return jedisCluster.zrevrangeByScoreWithScores(key, max, min);
	}

	public Set<String> zrevrangeByScore(String key, String max, String min) {
		return jedisCluster.zrevrangeByScore(key, max, min);
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
		return jedisCluster.zrangeByScoreWithScores(key, min, max, offset, count);
	}

	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		return jedisCluster.zrangeByScore(key, min, max, offset, count);
	}

	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
		return jedisCluster.zrevrangeByScore(key, max, min, offset, count);
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return jedisCluster.zrevrangeByScore(key, max, min, offset, count);
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		return jedisCluster.zrangeByScoreWithScores(key, min, max);
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
		return jedisCluster.zrangeByScoreWithScores(key, min, max);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
		return jedisCluster.zrevrangeByScoreWithScores(key, max, min);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
		return jedisCluster.zrevrangeByScoreWithScores(key, max, min);
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		return jedisCluster.zrangeByScoreWithScores(key, min, max, offset, count);
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return jedisCluster.zrangeByScoreWithScores(key, min, max, offset, count);
	}

	public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
		return jedisCluster.zrevrangeByScore(key, max, min, offset, count);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
		return jedisCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		return jedisCluster.zrangeByScoreWithScores(key, min, max);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return jedisCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
		return jedisCluster.zrevrangeByScoreWithScores(key, max, min);
	}

	public Long zremrangeByRank(byte[] key, long start, long end) {
		return jedisCluster.zremrangeByRank(key, start, end);
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		return jedisCluster.zrangeByScoreWithScores(key, min, max, offset, count);
	}

	public Long zremrangeByScore(byte[] key, double start, double end) {
		return jedisCluster.zremrangeByScore(key, start, end);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
		return jedisCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
		return jedisCluster.zremrangeByScore(key, start, end);
	}

	public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot, byte[] value) {
		return jedisCluster.linsert(key, where, pivot, value);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
		return jedisCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	public Long lpushx(byte[] key, byte[]... arg) {
		return jedisCluster.lpushx(key, arg);
	}

	public Long zremrangeByRank(String key, long start, long end) {
		return jedisCluster.zremrangeByRank(key, start, end);
	}

	public Long rpushx(byte[] key, byte[]... arg) {
		return jedisCluster.rpushx(key, arg);
	}

	public Long zremrangeByScore(String key, double start, double end) {
		return jedisCluster.zremrangeByScore(key, start, end);
	}

	public Long del(byte[] key) {
		return jedisCluster.del(key);
	}

	public Long zremrangeByScore(String key, String start, String end) {
		return jedisCluster.zremrangeByScore(key, start, end);
	}

	public byte[] echo(byte[] arg) {
		return jedisCluster.echo(arg);
	}

	public Long zlexcount(String key, String min, String max) {
		return jedisCluster.zlexcount(key, min, max);
	}

	public Long bitcount(byte[] key) {
		return jedisCluster.bitcount(key);
	}

	public Set<String> zrangeByLex(String key, String min, String max) {
		return jedisCluster.zrangeByLex(key, min, max);
	}

	public Long bitcount(byte[] key, long start, long end) {
		return jedisCluster.bitcount(key, start, end);
	}

	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
		return jedisCluster.zrangeByLex(key, min, max, offset, count);
	}

	public Long pfadd(byte[] key, byte[]... elements) {
		return jedisCluster.pfadd(key, elements);
	}

	public long pfcount(byte[] key) {
		return jedisCluster.pfcount(key);
	}

	public Set<String> zrevrangeByLex(String key, String max, String min) {
		return jedisCluster.zrevrangeByLex(key, max, min);
	}

	public List<byte[]> srandmember(byte[] key, int count) {
		return jedisCluster.srandmember(key, count);
	}

	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		return jedisCluster.zrevrangeByLex(key, max, min, offset, count);
	}

	public Long zlexcount(byte[] key, byte[] min, byte[] max) {
		return jedisCluster.zlexcount(key, min, max);
	}

	public Long zremrangeByLex(String key, String min, String max) {
		return jedisCluster.zremrangeByLex(key, min, max);
	}

	public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
		return jedisCluster.zrangeByLex(key, min, max);
	}

	public Long linsert(String key, LIST_POSITION where, String pivot, String value) {
		return jedisCluster.linsert(key, where, pivot, value);
	}

	public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return jedisCluster.zrangeByLex(key, min, max, offset, count);
	}

	public Long lpushx(String key, String... string) {
		return jedisCluster.lpushx(key, string);
	}

	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
		return jedisCluster.zrevrangeByLex(key, max, min);
	}

	public Long rpushx(String key, String... string) {
		return jedisCluster.rpushx(key, string);
	}

	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return jedisCluster.zrevrangeByLex(key, max, min, offset, count);
	}

	public Long del(String key) {
		return jedisCluster.del(key);
	}

	public String echo(String string) {
		return jedisCluster.echo(string);
	}

	public Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
		return jedisCluster.zremrangeByLex(key, min, max);
	}

	public Long bitcount(String key) {
		return jedisCluster.bitcount(key);
	}

	public Object eval(byte[] script, byte[] keyCount, byte[]... params) {
		return jedisCluster.eval(script, keyCount, params);
	}

	public Long bitcount(String key, long start, long end) {
		return jedisCluster.bitcount(key, start, end);
	}

	public Object eval(byte[] script, int keyCount, byte[]... params) {
		return jedisCluster.eval(script, keyCount, params);
	}

	public ScanResult<String> scan(String cursor, ScanParams params) {
		return jedisCluster.scan(cursor, params);
	}

	public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
		return jedisCluster.eval(script, keys, args);
	}

	public Object eval(byte[] script, byte[] key) {
		return jedisCluster.eval(script, key);
	}

	public Object evalsha(byte[] script, byte[] key) {
		return jedisCluster.evalsha(script, key);
	}

	public Long bitpos(String key, boolean value) {
		return jedisCluster.bitpos(key, value);
	}

	public Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
		return jedisCluster.evalsha(sha1, keys, args);
	}

	public Long bitpos(String key, boolean value, BitPosParams params) {
		return jedisCluster.bitpos(key, value, params);
	}

	public Object evalsha(byte[] sha1, int keyCount, byte[]... params) {
		return jedisCluster.evalsha(sha1, keyCount, params);
	}

	public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
		return jedisCluster.hscan(key, cursor);
	}

	public List<Long> scriptExists(byte[] key, byte[][] sha1) {
		return jedisCluster.scriptExists(key, sha1);
	}

	public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
		return jedisCluster.hscan(key, cursor, params);
	}

	public byte[] scriptLoad(byte[] script, byte[] key) {
		return jedisCluster.scriptLoad(script, key);
	}

	public ScanResult<String> sscan(String key, String cursor) {
		return jedisCluster.sscan(key, cursor);
	}

	public String scriptFlush(byte[] key) {
		return jedisCluster.scriptFlush(key);
	}

	public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
		return jedisCluster.sscan(key, cursor, params);
	}

	public String scriptKill(byte[] key) {
		return jedisCluster.scriptKill(key);
	}

	public ScanResult<Tuple> zscan(String key, String cursor) {
		return jedisCluster.zscan(key, cursor);
	}

	public Long del(byte[]... keys) {
		return jedisCluster.del(keys);
	}

	public List<byte[]> blpop(int timeout, byte[]... keys) {
		return jedisCluster.blpop(timeout, keys);
	}

	public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
		return jedisCluster.zscan(key, cursor, params);
	}

	public List<byte[]> brpop(int timeout, byte[]... keys) {
		return jedisCluster.brpop(timeout, keys);
	}

	public Long pfadd(String key, String... elements) {
		return jedisCluster.pfadd(key, elements);
	}

	public List<byte[]> mget(byte[]... keys) {
		return jedisCluster.mget(keys);
	}

	public long pfcount(String key) {
		return jedisCluster.pfcount(key);
	}

	public List<String> blpop(int timeout, String key) {
		return jedisCluster.blpop(timeout, key);
	}

	public String mset(byte[]... keysvalues) {
		return jedisCluster.mset(keysvalues);
	}

	public List<String> brpop(int timeout, String key) {
		return jedisCluster.brpop(timeout, key);
	}

	public Long msetnx(byte[]... keysvalues) {
		return jedisCluster.msetnx(keysvalues);
	}

	public Long del(String... keys) {
		return jedisCluster.del(keys);
	}

	public List<String> blpop(int timeout, String... keys) {
		return jedisCluster.blpop(timeout, keys);
	}

	public String rename(byte[] oldkey, byte[] newkey) {
		return jedisCluster.rename(oldkey, newkey);
	}

	public List<String> brpop(int timeout, String... keys) {
		return jedisCluster.brpop(timeout, keys);
	}

	public Long renamenx(byte[] oldkey, byte[] newkey) {
		return jedisCluster.renamenx(oldkey, newkey);
	}

	public List<String> mget(String... keys) {
		return jedisCluster.mget(keys);
	}

	public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
		return jedisCluster.rpoplpush(srckey, dstkey);
	}

	public String mset(String... keysvalues) {
		return jedisCluster.mset(keysvalues);
	}

	public Set<byte[]> sdiff(byte[]... keys) {
		return jedisCluster.sdiff(keys);
	}

	public Long sdiffstore(byte[] dstkey, byte[]... keys) {
		return jedisCluster.sdiffstore(dstkey, keys);
	}

	public Long msetnx(String... keysvalues) {
		return jedisCluster.msetnx(keysvalues);
	}

	public Set<byte[]> sinter(byte[]... keys) {
		return jedisCluster.sinter(keys);
	}

	public String rename(String oldkey, String newkey) {
		return jedisCluster.rename(oldkey, newkey);
	}

	public Long sinterstore(byte[] dstkey, byte[]... keys) {
		return jedisCluster.sinterstore(dstkey, keys);
	}

	public Long renamenx(String oldkey, String newkey) {
		return jedisCluster.renamenx(oldkey, newkey);
	}

	public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
		return jedisCluster.smove(srckey, dstkey, member);
	}

	public String rpoplpush(String srckey, String dstkey) {
		return jedisCluster.rpoplpush(srckey, dstkey);
	}

	public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
		return jedisCluster.sort(key, sortingParameters, dstkey);
	}

	public Set<String> sdiff(String... keys) {
		return jedisCluster.sdiff(keys);
	}

	public Long sdiffstore(String dstkey, String... keys) {
		return jedisCluster.sdiffstore(dstkey, keys);
	}

	public Long sort(byte[] key, byte[] dstkey) {
		return jedisCluster.sort(key, dstkey);
	}

	public Set<byte[]> sunion(byte[]... keys) {
		return jedisCluster.sunion(keys);
	}

	public Set<String> sinter(String... keys) {
		return jedisCluster.sinter(keys);
	}

	public Long sunionstore(byte[] dstkey, byte[]... keys) {
		return jedisCluster.sunionstore(dstkey, keys);
	}

	public Long sinterstore(String dstkey, String... keys) {
		return jedisCluster.sinterstore(dstkey, keys);
	}

	public Long zinterstore(byte[] dstkey, byte[]... sets) {
		return jedisCluster.zinterstore(dstkey, sets);
	}

	public Long smove(String srckey, String dstkey, String member) {
		return jedisCluster.smove(srckey, dstkey, member);
	}

	public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
		return jedisCluster.zinterstore(dstkey, params, sets);
	}

	public Long sort(String key, SortingParams sortingParameters, String dstkey) {
		return jedisCluster.sort(key, sortingParameters, dstkey);
	}

	public Long sort(String key, String dstkey) {
		return jedisCluster.sort(key, dstkey);
	}

	public Long zunionstore(byte[] dstkey, byte[]... sets) {
		return jedisCluster.zunionstore(dstkey, sets);
	}

	public Set<String> sunion(String... keys) {
		return jedisCluster.sunion(keys);
	}

	public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
		return jedisCluster.zunionstore(dstkey, params, sets);
	}

	public Long sunionstore(String dstkey, String... keys) {
		return jedisCluster.sunionstore(dstkey, keys);
	}

	public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
		return jedisCluster.brpoplpush(source, destination, timeout);
	}

	public Long zinterstore(String dstkey, String... sets) {
		return jedisCluster.zinterstore(dstkey, sets);
	}

	public Long publish(byte[] channel, byte[] message) {
		return jedisCluster.publish(channel, message);
	}

	public Long zinterstore(String dstkey, ZParams params, String... sets) {
		return jedisCluster.zinterstore(dstkey, params, sets);
	}

	public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
		jedisCluster.subscribe(jedisPubSub, channels);
	}

	public Long zunionstore(String dstkey, String... sets) {
		return jedisCluster.zunionstore(dstkey, sets);
	}

	public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
		jedisCluster.psubscribe(jedisPubSub, patterns);
	}

	public Long zunionstore(String dstkey, ZParams params, String... sets) {
		return jedisCluster.zunionstore(dstkey, params, sets);
	}

	public Long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
		return jedisCluster.bitop(op, destKey, srcKeys);
	}

	public String brpoplpush(String source, String destination, int timeout) {
		return jedisCluster.brpoplpush(source, destination, timeout);
	}

	public String pfmerge(byte[] destkey, byte[]... sourcekeys) {
		return jedisCluster.pfmerge(destkey, sourcekeys);
	}

	public Long publish(String channel, String message) {
		return jedisCluster.publish(channel, message);
	}

	public Long pfcount(byte[]... keys) {
		return jedisCluster.pfcount(keys);
	}

	public void subscribe(JedisPubSub jedisPubSub, String... channels) {
		jedisCluster.subscribe(jedisPubSub, channels);
	}

	public String ping() {
		return jedisCluster.ping();
	}

	public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
		jedisCluster.psubscribe(jedisPubSub, patterns);
	}

	public String quit() {
		return jedisCluster.quit();
	}

	public Long bitop(BitOP op, String destKey, String... srcKeys) {
		return jedisCluster.bitop(op, destKey, srcKeys);
	}

	public String flushDB() {
		return jedisCluster.flushDB();
	}

	public Long dbSize() {
		return jedisCluster.dbSize();
	}

	public String pfmerge(String destkey, String... sourcekeys) {
		return jedisCluster.pfmerge(destkey, sourcekeys);
	}

	public String select(int index) {
		return jedisCluster.select(index);
	}

	public long pfcount(String... keys) {
		return jedisCluster.pfcount(keys);
	}

	public String flushAll() {
		return jedisCluster.flushAll();
	}

	public Object eval(String script, int keyCount, String... params) {
		return jedisCluster.eval(script, keyCount, params);
	}

	public String auth(String password) {
		return jedisCluster.auth(password);
	}

	public Object eval(String script, String key) {
		return jedisCluster.eval(script, key);
	}

	public String save() {
		return jedisCluster.save();
	}

	public Object eval(String script, List<String> keys, List<String> args) {
		return jedisCluster.eval(script, keys, args);
	}

	public String bgsave() {
		return jedisCluster.bgsave();
	}

	public Object evalsha(String sha1, int keyCount, String... params) {
		return jedisCluster.evalsha(sha1, keyCount, params);
	}

	public String bgrewriteaof() {
		return jedisCluster.bgrewriteaof();
	}

	public Object evalsha(String sha1, List<String> keys, List<String> args) {
		return jedisCluster.evalsha(sha1, keys, args);
	}

	public Long lastsave() {
		return jedisCluster.lastsave();
	}

	public Object evalsha(String script, String key) {
		return jedisCluster.evalsha(script, key);
	}

	public String shutdown() {
		return jedisCluster.shutdown();
	}

	public Boolean scriptExists(String sha1, String key) { // hàm này chả lẽ bên single ko có??
		return jedisCluster.scriptExists(sha1, key);
	}

	public String info() {
		return jedisCluster.info();
	}

	public List<Boolean> scriptExists(String key, String... sha1) {
		return jedisCluster.scriptExists(key, sha1);
	}

	public String info(String section) {
		return jedisCluster.info(section);
	}

	public String scriptLoad(String script, String key) {
		return jedisCluster.scriptLoad(script, key);
	}

	public String slaveof(String host, int port) {
		return jedisCluster.slaveof(host, port);
	}

	public String set(String key, String value, String nxxx) {
		return jedisCluster.set(key, value, nxxx);
	}

	public String slaveofNoOne() {
		return jedisCluster.slaveofNoOne();
	}

	public Long getDB() {
		return jedisCluster.getDB();
	}

	public List<String> blpop(String arg) {
		return jedisCluster.blpop(arg);
	}

	public String debug(DebugParams params) {
		return jedisCluster.debug(params);
	}

	public List<String> brpop(String arg) {
		return jedisCluster.brpop(arg);
	}

	public String configResetStat() {
		return jedisCluster.configResetStat();
	}

	public Long move(String key, int dbIndex) {
		return jedisCluster.move(key, dbIndex);
	}

	public Long waitReplicas(int replicas, long timeout) {
		return jedisCluster.waitReplicas(replicas, timeout);
	}

	public Long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
		return jedisCluster.geoadd(key, longitude, latitude, member);
	}

	public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
		return jedisCluster.hscan(key, cursor);
	}

	public Long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
		return jedisCluster.geoadd(key, memberCoordinateMap);
	}

	public ScanResult<String> sscan(String key, int cursor) {
		return jedisCluster.sscan(key, cursor);
	}

	public Double geodist(byte[] key, byte[] member1, byte[] member2) {
		return jedisCluster.geodist(key, member1, member2);
	}

	public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
		return jedisCluster.geodist(key, member1, member2, unit);
	}

	public ScanResult<Tuple> zscan(String key, int cursor) {
		return jedisCluster.zscan(key, cursor);
	}

	public List<byte[]> geohash(byte[] key, byte[]... members) {
		return jedisCluster.geohash(key, members);
	}

	public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
		return jedisCluster.geopos(key, members);
	}

	public Long geoadd(String key, double longitude, double latitude, String member) {
		return jedisCluster.geoadd(key, longitude, latitude, member);
	}

	public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		return jedisCluster.georadius(key, longitude, latitude, radius, unit);
	}

	public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
		return jedisCluster.geoadd(key, memberCoordinateMap);
	}

	public Double geodist(String key, String member1, String member2) {
		return jedisCluster.geodist(key, member1, member2);
	}

	public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		return jedisCluster.georadius(key, longitude, latitude, radius, unit, param);
	}

	public Double geodist(String key, String member1, String member2, GeoUnit unit) {
		return jedisCluster.geodist(key, member1, member2, unit);
	}

	public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
		return jedisCluster.georadiusByMember(key, member, radius, unit);
	}

	public List<String> geohash(String key, String... members) {
		return jedisCluster.geohash(key, members);
	}

	public List<GeoCoordinate> geopos(String key, String... members) {
		return jedisCluster.geopos(key, members);
	}

	public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		return jedisCluster.georadiusByMember(key, member, radius, unit, param);
	}

	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		return jedisCluster.georadius(key, longitude, latitude, radius, unit);
	}

	public ScanResult<byte[]> scan(byte[] cursor, ScanParams params) {
		return jedisCluster.scan(cursor, params);
	}

	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		return jedisCluster.georadius(key, longitude, latitude, radius, unit, param);
	}

	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
		return jedisCluster.georadiusByMember(key, member, radius, unit);
	}

	public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor) {
		return jedisCluster.hscan(key, cursor);
	}

	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		return jedisCluster.georadiusByMember(key, member, radius, unit, param);
	}

	public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
		return jedisCluster.hscan(key, cursor, params);
	}

	public List<Long> bitfield(String key, String... arguments) {
		return jedisCluster.bitfield(key, arguments);
	}

	public ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
		return jedisCluster.sscan(key, cursor);
	}

	public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
		return jedisCluster.sscan(key, cursor, params);
	}

	public ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
		return jedisCluster.zscan(key, cursor);
	}

	public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
		return jedisCluster.zscan(key, cursor, params);
	}

	public List<byte[]> bitfield(byte[] key, byte[]... arguments) {
		return jedisCluster.bitfield(key, arguments);
	}

	@Override
	public Set<String> keys(String pattern) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String randomKey() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Set<byte[]> keys(byte[] pattern) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public byte[] randomBinaryKey() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long move(byte[] key, int dbIndex) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String watch(String... keys) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<String> blpop(String... args) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<String> brpop(String... args) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Transaction multi() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<Object> multi(TransactionBlock jedisTransaction) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String watch(byte[]... keys) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String unwatch() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<byte[]> blpop(byte[] arg) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<byte[]> brpop(byte[] arg) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<byte[]> blpop(byte[]... args) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<byte[]> brpop(byte[]... args) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<Object> pipelined(PipelineBlock jedisPipeline) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Pipeline pipelined() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<String> configGet(String pattern) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String configSet(String parameter, String value) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Object eval(String script) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Object evalsha(String script) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Boolean scriptExists(String sha1) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<Boolean> scriptExists(String... sha1) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String scriptLoad(String script) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<Slowlog> slowlogGet() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<Slowlog> slowlogGet(long entries) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long objectRefcount(String string) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String objectEncoding(String string) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long objectIdletime(String string) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<Map<String, String>> sentinelMasters() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<String> sentinelGetMasterAddrByName(String masterName) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long sentinelReset(String pattern) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<Map<String, String>> sentinelSlaves(String masterName) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public void monitor(JedisMonitor jedisMonitor) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String sentinelFailover(String masterName) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String sentinelMonitor(String masterName, String ip, int port, int quorum) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String sentinelRemove(String masterName) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String sentinelSet(String masterName, Map<String, String> parameterMap) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public byte[] dump(String key) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String restore(String key, int ttl, byte[] serializedValue) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long pexpire(String key, int milliseconds) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<byte[]> configGet(byte[] pattern) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String psetex(String key, int milliseconds, String value) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public byte[] configSet(byte[] parameter, byte[] value) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String set(String key, String value, String nxxx, String expx, int time) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clientKill(String client) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clientSetname(String name) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String migrate(String host, int port, String key, int destinationDb, int timeout) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public ScanResult<String> scan(int cursor) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public ScanResult<String> scan(int cursor, ScanParams params) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public void sync() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, int cursor, ScanParams params) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Client getClient() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public ScanResult<String> sscan(String key, int cursor, ScanParams params) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long bitpos(byte[] key, boolean value) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long bitpos(byte[] key, boolean value, BitPosParams params) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public ScanResult<Tuple> zscan(String key, int cursor, ScanParams params) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public ScanResult<String> scan(String cursor) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Object eval(byte[] script) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Object evalsha(byte[] sha1) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String scriptFlush() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long scriptExists(byte[] sha1) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<Long> scriptExists(byte[]... sha1) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public byte[] scriptLoad(byte[] script) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String scriptKill() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String slowlogReset() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long slowlogLen() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<byte[]> slowlogGetBinary() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<byte[]> slowlogGetBinary(long entries) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long objectRefcount(byte[] key) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public byte[] objectEncoding(byte[] key) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long objectIdletime(byte[] key) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterNodes() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public byte[] dump(byte[] key) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String readonly() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String restore(byte[] key, int ttl, byte[] serializedValue) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterMeet(String ip, int port) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterReset(Reset resetType) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long pexpire(byte[] key, int milliseconds) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterAddSlots(int... slots) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterDelSlots(int... slots) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterInfo() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long pttl(byte[] key) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<String> clusterGetKeysInSlot(int slot, int count) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String psetex(byte[] key, int milliseconds, byte[] value) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String psetex(byte[] key, long milliseconds, byte[] value) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterSetSlotNode(int slot, String nodeId) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterSetSlotMigrating(int slot, String nodeId) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterSetSlotImporting(int slot, String nodeId) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String set(byte[] key, byte[] value, byte[] nxxx) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterSetSlotStable(int slot) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, int time) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterForget(String nodeId) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clientKill(byte[] client) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterFlushSlots() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long clusterKeySlot(String key) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clientGetname() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clientList() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long clusterCountKeysInSlot(int slot) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clientSetname(byte[] name) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterSaveConfig() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<String> time() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterReplicate(String nodeId) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String migrate(byte[] host, int port, byte[] key, int destinationDb, int timeout) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<String> clusterSlaves(String nodeId) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String clusterFailover() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<Object> clusterSlots() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public String asking() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public List<String> pubsubChannels(String pattern) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Long pubsubNumPat() {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public Map<String, String> pubsubNumSub(String... channels) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");

	}

	@Override
	public ScanResult<byte[]> scan(byte[] cursor) {
		throw new UnsupportedOperationException("Method not supported in Redis Cluster");
	}
}
