package io.gridgo.connector.redis.adapter.lettuce;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.redis.adapter.RedisConfig;
import io.gridgo.connector.redis.adapter.RedisType;
import io.gridgo.utils.support.HostAndPort;
import io.lettuce.core.BitFieldArgs;
import io.lettuce.core.Consumer;
import io.lettuce.core.GeoArgs;
import io.lettuce.core.GeoArgs.Unit;
import io.lettuce.core.GeoRadiusStoreArgs;
import io.lettuce.core.KillArgs;
import io.lettuce.core.Limit;
import io.lettuce.core.MigrateArgs;
import io.lettuce.core.Range;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.RedisURI.Builder;
import io.lettuce.core.RestoreArgs;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.SortArgs;
import io.lettuce.core.StreamScanCursor;
import io.lettuce.core.UnblockType;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.XClaimArgs;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XReadArgs.StreamOffset;
import io.lettuce.core.ZAddArgs;
import io.lettuce.core.ZStoreArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.ScoredValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingChannel;
import io.lettuce.core.protocol.CommandType;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public class LettuceSingleClient extends AbstractLettuceClient {

    private StatefulRedisConnection<byte[], byte[]> connection;

    private RedisAsyncCommands<byte[], byte[]> commands;

    protected LettuceSingleClient(RedisConfig config) {
        super(RedisType.SINGLE, config);
    }

    protected LettuceSingleClient(RedisType redisType, RedisConfig config) {
        super(redisType, config);
    }

    protected StatefulRedisConnection<byte[], byte[]> createConnection() {
        RedisConfig config = this.getConfig();

        HostAndPort hostAndPort = config.getAddress().getFirst();
        Builder builder = (hostAndPort.getPort() > 0) //
                ? RedisURI.Builder.redis(hostAndPort.getHost(), hostAndPort.getPort()) //
                : RedisURI.Builder.redis(hostAndPort.getHost());

        if (config.getPassword() != null) {
            builder.withPassword(config.getPassword());
        }

        if (config.getDatabase() >= 0) {
            builder.withDatabase(config.getDatabase());
        }

        StatefulRedisConnection<byte[], byte[]> connection = RedisClient.create(builder.build()).connect(this.getCodec());
        return connection;
    }

    @Override
    protected void onStop() {
        this.connection.close();
    }

    @Override
    protected void onStart() {
        this.connection = this.createConnection();
        this.commands = connection.async();
    }

    @Override
    public Promise<BElement, Exception> geoadd(byte[] key, double longitude, double latitude, byte[] member) {
        return toPromise(commands.geoadd(key, longitude, latitude, member));
    }

    @Override
    public Promise<BElement, Exception> pfadd(byte[] key, byte[]... values) {
        return toPromise(commands.pfadd(key, values));
    }

    @Override
    public Promise<BElement, Exception> discard() {
        return toPromise(commands.discard());
    }

    @Override
    public Promise<BElement, Exception> xack(byte[] key, byte[] group, String... messageIds) {
        return toPromise(commands.xack(key, group, messageIds));
    }

    @Override
    public Promise<BElement, Exception> sadd(byte[] key, byte[]... members) {
        return toPromise(commands.sadd(key, members));
    }

    @Override
    public Promise<BElement, Exception> blpop(long timeout, byte[]... keys) {
        return toPromise(commands.blpop(timeout, keys));
    }

    @Override
    public Promise<BElement, Exception> bzpopmin(long timeout, byte[]... keys) {
        return toPromise(commands.bzpopmin(timeout, keys));
    }

    @Override
    public Promise<BElement, Exception> bgrewriteaof() {
        return toPromise(commands.bgrewriteaof());
    }

    @Override
    public Promise<BElement, Exception> publish(byte[] channel, byte[] message) {
        return toPromise(commands.publish(channel, message));
    }

    @Override
    public Promise<BElement, Exception> append(byte[] key, byte[] value) {
        return toPromise(commands.append(key, value));
    }

    @Override
    public Promise<BElement, Exception> hdel(byte[] key, byte[]... fields) {
        return toPromise(commands.hdel(key, fields));
    }

    @Override
    public Promise<BElement, Exception> exec() {
        return toPromise(commands.exec());
    }

    @Override
    public Promise<BElement, Exception> unlink(byte[]... keys) {
        return toPromise(commands.unlink(keys));
    }

    @Override
    public Promise<BElement, Exception> pfmerge(byte[] destkey, byte[]... sourcekeys) {
        return toPromise(commands.pfmerge(destkey, sourcekeys));
    }

    @Override
    public Promise<BElement, Exception> bgsave() {
        return toPromise(commands.bgsave());
    }

    @Override
    public Promise<BElement, Exception> eval(String script, String type, byte[][] keys, byte[]... values) {
        return toPromise(commands.eval(script, ScriptOutputType.valueOf(type.trim().toUpperCase()), keys, values));
    }

    @Override
    public Promise<BElement, Exception> geoadd(byte[] key, Object... lngLatMember) {
        return toPromise(commands.geoadd(key, lngLatMember));
    }

    @Override
    public Promise<BElement, Exception> xadd(byte[] key, Map<byte[], byte[]> body) {
        return toPromise(commands.xadd(key, body));
    }

    @Override
    public Promise<BElement, Exception> scard(byte[] key) {
        return toPromise(commands.scard(key));
    }

    @Override
    public Promise<BElement, Exception> bitcount(byte[] key) {
        return toPromise(commands.bitcount(key));
    }

    @Override
    public Promise<BElement, Exception> pubsubChannels() {
        return toPromise(commands.pubsubChannels());
    }

    @Override
    public Promise<BElement, Exception> clientGetname() {
        return toPromise(commands.clientGetname());
    }

    @Override
    public Promise<BElement, Exception> multi() {
        return toPromise(commands.multi());
    }

    @Override
    public Promise<BElement, Exception> hexists(byte[] key, byte[] field) {
        return toPromise(commands.hexists(key, field));
    }

    @Override
    public Promise<BElement, Exception> bzpopmax(long timeout, byte[]... keys) {
        return toPromise(commands.bzpopmax(timeout, keys));
    }

    public String auth(String password) {
        return commands.auth(password);
    }

    public void setTimeout(Duration timeout) {
        commands.setTimeout(timeout);
    }

    @Override
    public Promise<BElement, Exception> dump(byte[] key) {
        return toPromise(commands.dump(key));
    }

    @Override
    public Promise<BElement, Exception> bitcount(byte[] key, long start, long end) {
        return toPromise(commands.bitcount(key, start, end));
    }

    @Override
    public Promise<BElement, Exception> xadd(byte[] key, XAddArgs args, Map<byte[], byte[]> body) {
        return toPromise(commands.xadd(key, args, body));
    }

    @Override
    public Promise<BElement, Exception> pfcount(byte[]... keys) {
        return toPromise(commands.pfcount(keys));
    }

    @Override
    public Promise<BElement, Exception> pubsubChannels(byte[] channel) {
        return toPromise(commands.pubsubChannels(channel));
    }

    @Override
    public Promise<BElement, Exception> watch(byte[]... keys) {
        return toPromise(commands.watch(keys));
    }

    @Override
    public Promise<BElement, Exception> sdiff(byte[]... keys) {
        return toPromise(commands.sdiff(keys));
    }

    @Override
    public Promise<BElement, Exception> clientSetname(byte[] name) {
        return toPromise(commands.clientSetname(name));
    }

    @Override
    public Promise<BElement, Exception> brpop(long timeout, byte[]... keys) {
        return toPromise(commands.brpop(timeout, keys));
    }

    @Override
    public Promise<BElement, Exception> evalsha(String digest, String outputType, byte[]... keys) {
        ScriptOutputType type = ScriptOutputType.valueOf(outputType.trim().toUpperCase());
        return toPromise(commands.evalsha(digest, type, keys));
    }

    @Override
    public Promise<BElement, Exception> geohash(byte[] key, byte[]... members) {
        return toPromise(commands.geohash(key, members));
    }

    public String select(int db) {
        return commands.select(db);
    }

    @Override
    public Promise<BElement, Exception> exists(byte[]... keys) {
        return toPromise(commands.exists(keys));
    }

    @Override
    public Promise<BElement, Exception> sdiff(ValueStreamingChannel<byte[]> channel, byte[]... keys) {
        return toPromise(commands.sdiff(channel, keys));
    }

    @Override
    public Promise<BElement, Exception> bitfield(byte[] key, BitFieldArgs bitFieldArgs) {
        return toPromise(commands.bitfield(key, bitFieldArgs));
    }

    @Override
    public Promise<BElement, Exception> swapdb(int db1, int db2) {
        return toPromise(commands.swapdb(db1, db2));
    }

    @Override
    public Promise<BElement, Exception> unwatch() {
        return toPromise(commands.unwatch());
    }

    @Override
    public Promise<BElement, Exception> xadd(byte[] key, Object... keysAndValues) {
        return toPromise(commands.xadd(key, keysAndValues));
    }

    @Override
    public Promise<BElement, Exception> clientKill(String addr) {
        return toPromise(commands.clientKill(addr));
    }

    @Override
    public Promise<BElement, Exception> hget(byte[] key, byte[] field) {
        return toPromise(commands.hget(key, field));
    }

    @Override
    public Promise<BElement, Exception> pubsubNumsub(byte[]... channels) {
        return toPromise(commands.pubsubNumsub(channels));
    }

    @Override
    public Promise<BElement, Exception> zadd(byte[] key, double score, byte[] member) {
        return toPromise(commands.zadd(key, score, member));
    }

    @Override
    public Promise<BElement, Exception> expire(byte[] key, long seconds) {
        return toPromise(commands.expire(key, seconds));
    }

    @Override
    public Promise<BElement, Exception> evalsha(String digest, String outputType, byte[][] keys, byte[]... values) {
        ScriptOutputType type = ScriptOutputType.valueOf(outputType.trim().toUpperCase());
        return toPromise(commands.evalsha(digest, type, keys, values));
    }

    @Override
    public Promise<BElement, Exception> sdiffstore(byte[] destination, byte[]... keys) {
        return toPromise(commands.sdiffstore(destination, keys));
    }

    @Override
    public Promise<BElement, Exception> xadd(byte[] key, XAddArgs args, Object... keysAndValues) {
        return toPromise(commands.xadd(key, args, keysAndValues));
    }

    @Override
    public Promise<BElement, Exception> clusterBumpepoch() {
        return toPromise(commands.clusterBumpepoch());
    }

    @Override
    public Promise<BElement, Exception> clientKill(KillArgs killArgs) {
        return toPromise(commands.clientKill(killArgs));
    }

    @Override
    public Promise<BElement, Exception> bitpos(byte[] key, boolean state) {
        return toPromise(commands.bitpos(key, state));
    }

    @Override
    public Promise<BElement, Exception> pubsubNumpat() {
        return toPromise(commands.pubsubNumpat());
    }

    @Override
    public Promise<BElement, Exception> hincrby(byte[] key, byte[] field, long amount) {
        return toPromise(commands.hincrby(key, field, amount));
    }

    @Override
    public Promise<BElement, Exception> brpoplpush(long timeout, byte[] source, byte[] destination) {
        return toPromise(commands.brpoplpush(timeout, source, destination));
    }

    @Override
    public Promise<BElement, Exception> expireat(byte[] key, Date timestamp) {
        return toPromise(commands.expireat(key, timestamp));
    }

    @Override
    public Promise<BElement, Exception> scriptExists(String... digests) {
        return toPromise(commands.scriptExists(digests));
    }

    @Override
    public Promise<BElement, Exception> sinter(byte[]... keys) {
        return toPromise(commands.sinter(keys));
    }

    @Override
    public Promise<BElement, Exception> clientUnblock(long id, UnblockType type) {
        return toPromise(commands.clientUnblock(id, type));
    }

    @Override
    public Promise<BElement, Exception> zadd(byte[] key, Object... scoresAndValues) {
        return toPromise(commands.zadd(key, scoresAndValues));
    }

    @Override
    public Promise<BElement, Exception> xclaim(byte[] key, Consumer<byte[]> consumer, long minIdleTime, String... messageIds) {
        return toPromise(commands.xclaim(key, consumer, minIdleTime, messageIds));
    }

    @Override
    public Promise<BElement, Exception> echo(byte[] msg) {
        return toPromise(commands.echo(msg));
    }

    @Override
    public Promise<BElement, Exception> clusterMeet(String ip, int port) {
        return toPromise(commands.clusterMeet(ip, port));
    }

    @Override
    public Promise<BElement, Exception> role() {
        return toPromise(commands.role());
    }

    @Override
    public Promise<BElement, Exception> hincrbyfloat(byte[] key, byte[] field, double amount) {
        return toPromise(commands.hincrbyfloat(key, field, amount));
    }

    @Override
    public Promise<BElement, Exception> sinter(ValueStreamingChannel<byte[]> channel, byte[]... keys) {
        return toPromise(commands.sinter(channel, keys));
    }

    @Override
    public Promise<BElement, Exception> clientPause(long timeout) {
        return toPromise(commands.clientPause(timeout));
    }

    @Override
    public Promise<BElement, Exception> lindex(byte[] key, long index) {
        return toPromise(commands.lindex(key, index));
    }

    @Override
    public Promise<BElement, Exception> expireat(byte[] key, long timestamp) {
        return toPromise(commands.expireat(key, timestamp));
    }

    @Override
    public Promise<BElement, Exception> scriptFlush() {
        return toPromise(commands.scriptFlush());
    }

    @Override
    public Promise<BElement, Exception> sinterstore(byte[] destination, byte[]... keys) {
        return toPromise(commands.sinterstore(destination, keys));
    }

    @Override
    public Promise<BElement, Exception> ping() {
        return toPromise(commands.ping());
    }

    @Override
    public Promise<BElement, Exception> xclaim(byte[] key, Consumer<byte[]> consumer, XClaimArgs args, String... messageIds) {
        return toPromise(commands.xclaim(key, consumer, args, messageIds));
    }

    @Override
    public Promise<BElement, Exception> clusterForget(String nodeId) {
        return toPromise(commands.clusterForget(nodeId));
    }

    @Override
    public Promise<BElement, Exception> hgetall(byte[] key) {
        return toPromise(commands.hgetall(key));
    }

    @Override
    public Promise<BElement, Exception> clientList() {
        return toPromise(commands.clientList());
    }

    @Override
    public Promise<BElement, Exception> scriptKill() {
        return toPromise(commands.scriptKill());
    }

    @Override
    public Promise<BElement, Exception> readOnly() {
        return toPromise(commands.readOnly());
    }

    @Override
    public Promise<BElement, Exception> linsert(byte[] key, boolean before, byte[] pivot, byte[] value) {
        return toPromise(commands.linsert(key, before, pivot, value));
    }

    @Override
    public Promise<BElement, Exception> scriptLoad(byte[] script) {
        return toPromise(commands.scriptLoad(script));
    }

    @Override
    public Promise<BElement, Exception> sismember(byte[] key, byte[] member) {
        return toPromise(commands.sismember(key, member));
    }

    @Override
    public Promise<BElement, Exception> clusterAddSlots(int... slots) {
        return toPromise(commands.clusterAddSlots(slots));
    }

    @Override
    public Promise<BElement, Exception> readWrite() {
        return toPromise(commands.readWrite());
    }

    @Override
    public Promise<BElement, Exception> bitpos(byte[] key, boolean state, long start) {
        return toPromise(commands.bitpos(key, state, start));
    }

    @Override
    public Promise<BElement, Exception> keys(byte[] pattern) {
        return toPromise(commands.keys(pattern));
    }

    @Override
    public Promise<BElement, Exception> hgetall(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key) {
        return toPromise(commands.hgetall(channel, key));
    }

    @Override
    public Promise<BElement, Exception> georadius(byte[] key, double longitude, double latitude, double distance, @NonNull String unitStr, byte[] storeKey,
            byte[] storeDistKey, Long count, String sort) {
        GeoRadiusStoreArgs<byte[]> args = buildGeoRadiusStoreArgs(storeKey, storeDistKey, count, sort);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(commands.georadius(key, longitude, latitude, distance, unit, args));
    }

    @Override
    public Promise<BElement, Exception> georadius(byte[] key, double longitude, double latitude, double distance, String unitStr, boolean withdistance,
            boolean withcoordinates, boolean withHash, Long count, String sort) {
        GeoArgs geoArgs = buildGeoArgs(withdistance, withcoordinates, withHash, sort, count);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(commands.georadius(key, longitude, latitude, distance, unit, geoArgs));
    }

    @Override
    public Promise<BElement, Exception> georadiusbymember(byte[] key, byte[] member, double distance, @NonNull String unitStr, byte[] storeKey,
            byte[] storeDistKey, Long count, String sort) {
        GeoRadiusStoreArgs<byte[]> geoRadiusStoreArgs = buildGeoRadiusStoreArgs(storeKey, storeDistKey, count, sort);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(commands.georadiusbymember(key, member, distance, unit, geoRadiusStoreArgs));
    }

    @Override
    public Promise<BElement, Exception> georadiusbymember(byte[] key, byte[] member, double distance, String unitStr, boolean withdistance,
            boolean withcoordinates, boolean withHash, Long count, String sort) {
        GeoArgs geoArgs = buildGeoArgs(withdistance, withcoordinates, withHash, sort, count);
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(commands.georadiusbymember(key, member, distance, unit, geoArgs));
    }

    @Override
    public Promise<BElement, Exception> command() {
        return toPromise(commands.command());
    }

    @Override
    public Promise<BElement, Exception> quit() {
        return toPromise(commands.quit());
    }

    @Override
    public Promise<BElement, Exception> xdel(byte[] key, String... messageIds) {
        return toPromise(commands.xdel(key, messageIds));
    }

    @Override
    public Promise<BElement, Exception> clusterDelSlots(int... slots) {
        return toPromise(commands.clusterDelSlots(slots));
    }

    @Override
    public Promise<BElement, Exception> llen(byte[] key) {
        return toPromise(commands.llen(key));
    }

    @Override
    public Promise<BElement, Exception> keys(KeyStreamingChannel<byte[]> channel, byte[] pattern) {
        return toPromise(commands.keys(channel, pattern));
    }

    @Override
    public Promise<BElement, Exception> commandInfo(String... cmds) {
        return toPromise(commands.commandInfo(cmds));
    }

    @Override
    public Promise<BElement, Exception> hkeys(byte[] key) {
        return toPromise(commands.hkeys(key));
    }

    @Override
    public Promise<BElement, Exception> smove(byte[] source, byte[] destination, byte[] member) {
        return toPromise(commands.smove(source, destination, member));
    }

    @Override
    public Promise<BElement, Exception> lpop(byte[] key) {
        return toPromise(commands.lpop(key));
    }

    @Override
    public Promise<BElement, Exception> clusterSetSlotNode(int slot, String nodeId) {
        return toPromise(commands.clusterSetSlotNode(slot, nodeId));
    }

    @Override
    public Promise<BElement, Exception> commandInfo(CommandType... cmds) {
        return toPromise(commands.commandInfo(cmds));
    }

    @Override
    public Promise<BElement, Exception> waitForReplication(int replicas, long timeout) {
        return toPromise(commands.waitForReplication(replicas, timeout));
    }

    @Override
    public Promise<BElement, Exception> migrate(String host, int port, byte[] key, int db, long timeout) {
        return toPromise(commands.migrate(host, port, key, db, timeout));
    }

    @Override
    public Promise<BElement, Exception> hkeys(KeyStreamingChannel<byte[]> channel, byte[] key) {
        return toPromise(commands.hkeys(channel, key));
    }

    @Override
    public Promise<BElement, Exception> xgroupCreate(StreamOffset<byte[]> streamOffset, byte[] group) {
        return toPromise(commands.xgroupCreate(streamOffset, group));
    }

    @Override
    public Promise<BElement, Exception> lpush(byte[] key, byte[]... values) {
        return toPromise(commands.lpush(key, values));
    }

    @Override
    public Promise<BElement, Exception> commandCount() {
        return toPromise(commands.commandCount());
    }

    @Override
    public Promise<BElement, Exception> hlen(byte[] key) {
        return toPromise(commands.hlen(key));
    }

    @Override
    public Promise<BElement, Exception> clusterSetSlotStable(int slot) {
        return toPromise(commands.clusterSetSlotStable(slot));
    }

    @Override
    public Promise<BElement, Exception> smembers(byte[] key) {
        return toPromise(commands.smembers(key));
    }

    @Override
    public Promise<BElement, Exception> xgroupDelconsumer(byte[] key, Consumer<byte[]> consumer) {
        return toPromise(commands.xgroupDelconsumer(key, consumer));
    }

    @Override
    public Promise<BElement, Exception> lpushx(byte[] key, byte[]... values) {
        return toPromise(commands.lpushx(key, values));
    }

    @Override
    public Promise<BElement, Exception> migrate(String host, int port, int db, long timeout, MigrateArgs<byte[]> migrateArgs) {
        return toPromise(commands.migrate(host, port, db, timeout, migrateArgs));
    }

    @Override
    public Promise<BElement, Exception> configGet(String parameter) {
        return toPromise(commands.configGet(parameter));
    }

    @Override
    public Promise<BElement, Exception> hmget(byte[] key, byte[]... fields) {
        return toPromise(commands.hmget(key, fields));
    }

    @Override
    public Promise<BElement, Exception> smembers(ValueStreamingChannel<byte[]> channel, byte[] key) {
        return toPromise(commands.smembers(channel, key));
    }

    @Override
    public Promise<BElement, Exception> clusterSetSlotMigrating(int slot, String nodeId) {
        return toPromise(commands.clusterSetSlotMigrating(slot, nodeId));
    }

    @Override
    public Promise<BElement, Exception> bitpos(byte[] key, boolean state, long start, long end) {
        return toPromise(commands.bitpos(key, state, start, end));
    }

    @Override
    public Promise<BElement, Exception> xgroupDestroy(byte[] key, byte[] group) {
        return toPromise(commands.xgroupDestroy(key, group));
    }

    @Override
    public Promise<BElement, Exception> configResetstat() {
        return toPromise(commands.configResetstat());
    }

    @Override
    public Promise<BElement, Exception> lrange(byte[] key, long start, long stop) {
        return toPromise(commands.lrange(key, start, stop));
    }

    @Override
    public Promise<BElement, Exception> spop(byte[] key) {
        return toPromise(commands.spop(key));
    }

    @Override
    public Promise<BElement, Exception> hmget(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key, byte[]... fields) {
        return toPromise(commands.hmget(channel, key, fields));
    }

    @Override
    public Promise<BElement, Exception> configRewrite() {
        return toPromise(commands.configRewrite());
    }

    @Override
    public Promise<BElement, Exception> xgroupSetid(StreamOffset<byte[]> streamOffset, byte[] group) {
        return toPromise(commands.xgroupSetid(streamOffset, group));
    }

    @Override
    public Promise<BElement, Exception> move(byte[] key, int db) {
        return toPromise(commands.move(key, db));
    }

    @Override
    public Promise<BElement, Exception> clusterSetSlotImporting(int slot, String nodeId) {
        return toPromise(commands.clusterSetSlotImporting(slot, nodeId));
    }

    @Override
    public Promise<BElement, Exception> lrange(ValueStreamingChannel<byte[]> channel, byte[] key, long start, long stop) {
        return toPromise(commands.lrange(channel, key, start, stop));
    }

    @Override
    public Promise<BElement, Exception> spop(byte[] key, long count) {
        return toPromise(commands.spop(key, count));
    }

    @Override
    public Promise<BElement, Exception> hmset(byte[] key, Map<byte[], byte[]> map) {
        return toPromise(commands.hmset(key, map));
    }

    @Override
    public Promise<BElement, Exception> configSet(String parameter, String value) {
        return toPromise(commands.configSet(parameter, value));
    }

    @Override
    public Promise<BElement, Exception> objectEncoding(byte[] key) {
        return toPromise(commands.objectEncoding(key));
    }

    public boolean isOpen() {
        return commands.isOpen();
    }

    @Override
    public Promise<BElement, Exception> zaddincr(byte[] key, double score, byte[] member) {
        return toPromise(commands.zaddincr(key, score, member));
    }

    @Override
    public Promise<BElement, Exception> xlen(byte[] key) {
        return toPromise(commands.xlen(key));
    }

    public void reset() {
        commands.reset();
    }

    @Override
    public Promise<BElement, Exception> clusterInfo() {
        return toPromise(commands.clusterInfo());
    }

    @Override
    public Promise<BElement, Exception> hscan(byte[] key) {
        return toPromise(commands.hscan(key));
    }

    @Override
    public Promise<BElement, Exception> srandmember(byte[] key) {
        return toPromise(commands.srandmember(key));
    }

    @Override
    public Promise<BElement, Exception> objectIdletime(byte[] key) {
        return toPromise(commands.objectIdletime(key));
    }

    @Override
    public Promise<BElement, Exception> lrem(byte[] key, long count, byte[] value) {
        return toPromise(commands.lrem(key, count, value));
    }

    @Override
    public Promise<BElement, Exception> xpending(byte[] key, byte[] group) {
        return toPromise(commands.xpending(key, group));
    }

    @Override
    public Promise<BElement, Exception> dbsize() {
        return toPromise(commands.dbsize());
    }

    @Override
    public Promise<BElement, Exception> clusterMyId() {
        return toPromise(commands.clusterMyId());
    }

    public void setAutoFlushCommands(boolean autoFlush) {
        commands.setAutoFlushCommands(autoFlush);
    }

    @Override
    public Promise<BElement, Exception> hscan(byte[] key, ScanArgs scanArgs) {
        return toPromise(commands.hscan(key, scanArgs));
    }

    @Override
    public Promise<BElement, Exception> debugCrashAndRecover(Long delay) {
        return toPromise(commands.debugCrashAndRecover(delay));
    }

    @Override
    public Promise<BElement, Exception> lset(byte[] key, long index, byte[] value) {
        return toPromise(commands.lset(key, index, value));
    }

    @Override
    public Promise<BElement, Exception> xpending(byte[] key, byte[] group, Range<String> range, Limit limit) {
        return toPromise(commands.xpending(key, group, range, limit));
    }

    @Override
    public Promise<BElement, Exception> clusterNodes() {
        return toPromise(commands.clusterNodes());
    }

    @Override
    public Promise<BElement, Exception> objectRefcount(byte[] key) {
        return toPromise(commands.objectRefcount(key));
    }

    @Override
    public Promise<BElement, Exception> srandmember(byte[] key, long count) {
        return toPromise(commands.srandmember(key, count));
    }

    @Override
    public Promise<BElement, Exception> debugHtstats(int db) {
        return toPromise(commands.debugHtstats(db));
    }

    @Override
    public Promise<BElement, Exception> hscan(byte[] key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return toPromise(commands.hscan(key, scanCursor, scanArgs));
    }

    @Override
    public Promise<BElement, Exception> persist(byte[] key) {
        return toPromise(commands.persist(key));
    }

    @Override
    public Promise<BElement, Exception> ltrim(byte[] key, long start, long stop) {
        return toPromise(commands.ltrim(key, start, stop));
    }

    @Override
    public Promise<BElement, Exception> clusterSlaves(String nodeId) {
        return toPromise(commands.clusterSlaves(nodeId));
    }

    public void flushCommands() {
        commands.flushCommands();
    }

    @Override
    public Promise<BElement, Exception> debugObject(byte[] key) {
        return toPromise(commands.debugObject(key));
    }

    @Override
    public Promise<BElement, Exception> bitopAnd(byte[] destination, byte[]... keys) {
        return toPromise(commands.bitopAnd(destination, keys));
    }

    @Override
    public Promise<BElement, Exception> geopos(byte[] key, byte[]... members) {
        return toPromise(commands.geopos(key, members));
    }

    @Override
    public Promise<BElement, Exception> zcard(byte[] key) {
        return toPromise(commands.zcard(key));
    }

    @Override
    public Promise<BElement, Exception> srandmember(ValueStreamingChannel<byte[]> channel, byte[] key, long count) {
        return toPromise(commands.srandmember(channel, key, count));
    }

    @Override
    public Promise<BElement, Exception> xpending(byte[] key, Consumer<byte[]> consumer, Range<String> range, Limit limit) {
        return toPromise(commands.xpending(key, consumer, range, limit));
    }

    @Override
    public Promise<BElement, Exception> rpop(byte[] key) {
        return toPromise(commands.rpop(key));
    }

    @Override
    public Promise<BElement, Exception> hscan(byte[] key, ScanCursor scanCursor) {
        return toPromise(commands.hscan(key, scanCursor));
    }

    public void debugOom() {
        commands.debugOom();
    }

    @Override
    public Promise<BElement, Exception> pexpire(byte[] key, long milliseconds) {
        return toPromise(commands.pexpire(key, milliseconds));
    }

    public void debugSegfault() {
        commands.debugSegfault();
    }

    @Override
    public Promise<BElement, Exception> bitopNot(byte[] destination, byte[] source) {
        return toPromise(commands.bitopNot(destination, source));
    }

    @Override
    public Promise<BElement, Exception> clusterGetKeysInSlot(int slot, int count) {
        return toPromise(commands.clusterGetKeysInSlot(slot, count));
    }

    @Override
    public Promise<BElement, Exception> rpoplpush(byte[] source, byte[] destination) {
        return toPromise(commands.rpoplpush(source, destination));
    }

    @Override
    public Promise<BElement, Exception> geodist(byte[] key, byte[] from, byte[] to, String unitStr) {
        Unit unit = Unit.valueOf(unitStr.trim().toLowerCase());
        return toPromise(commands.geodist(key, from, to, unit));
    }

    @Override
    public Promise<BElement, Exception> srem(byte[] key, byte[]... members) {
        return toPromise(commands.srem(key, members));
    }

    @Override
    public Promise<BElement, Exception> debugReload() {
        return toPromise(commands.debugReload());
    }

    @Override
    public Promise<BElement, Exception> hscan(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key) {
        return toPromise(commands.hscan(channel, key));
    }

    @Override
    public Promise<BElement, Exception> xrange(byte[] key, Range<String> range) {
        return toPromise(commands.xrange(key, range));
    }

    @Override
    public Promise<BElement, Exception> pexpireat(byte[] key, Date timestamp) {
        return toPromise(commands.pexpireat(key, timestamp));
    }

    @Override
    public Promise<BElement, Exception> clusterCountKeysInSlot(int slot) {
        return toPromise(commands.clusterCountKeysInSlot(slot));
    }

    @Override
    public Promise<BElement, Exception> debugRestart(Long delay) {
        return toPromise(commands.debugRestart(delay));
    }

    @Override
    public Promise<BElement, Exception> rpush(byte[] key, byte[]... values) {
        return toPromise(commands.rpush(key, values));
    }

    @Override
    public Promise<BElement, Exception> sunion(byte[]... keys) {
        return toPromise(commands.sunion(keys));
    }

    @Override
    public Promise<BElement, Exception> bitopOr(byte[] destination, byte[]... keys) {
        return toPromise(commands.bitopOr(destination, keys));
    }

    @Override
    public Promise<BElement, Exception> hscan(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key, ScanArgs scanArgs) {
        return toPromise(commands.hscan(channel, key, scanArgs));
    }

    @Override
    public Promise<BElement, Exception> debugSdslen(byte[] key) {
        return toPromise(commands.debugSdslen(key));
    }

    @Override
    public Promise<BElement, Exception> xrange(byte[] key, Range<String> range, Limit limit) {
        return toPromise(commands.xrange(key, range, limit));
    }

    @Override
    public Promise<BElement, Exception> sunion(ValueStreamingChannel<byte[]> channel, byte[]... keys) {
        return toPromise(commands.sunion(channel, keys));
    }

    @Override
    public Promise<BElement, Exception> clusterCountFailureReports(String nodeId) {
        return toPromise(commands.clusterCountFailureReports(nodeId));
    }

    @Override
    public Promise<BElement, Exception> rpushx(byte[] key, byte[]... values) {
        return toPromise(commands.rpushx(key, values));
    }

    @Override
    public Promise<BElement, Exception> pexpireat(byte[] key, long timestamp) {
        return toPromise(commands.pexpireat(key, timestamp));
    }

    @Override
    public Promise<BElement, Exception> flushall() {
        return toPromise(commands.flushall());
    }

    @Override
    public Promise<BElement, Exception> bitopXor(byte[] destination, byte[]... keys) {
        return toPromise(commands.bitopXor(destination, keys));
    }

    @Override
    public Promise<BElement, Exception> zcount(byte[] key, boolean includeLower, long lower, long upper, boolean includeUpper) {
        return toPromise(commands.zcount(key, buildRangeLong(includeLower, lower, upper, includeUpper)));
    }

    @Override
    public Promise<BElement, Exception> hscan(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return toPromise(commands.hscan(channel, key, scanCursor, scanArgs));
    }

    @Override
    public Promise<BElement, Exception> sunionstore(byte[] destination, byte[]... keys) {
        return toPromise(commands.sunionstore(destination, keys));
    }

    @Override
    public Promise<BElement, Exception> flushallAsync() {
        return toPromise(commands.flushallAsync());
    }

    @Override
    public Promise<BElement, Exception> xread(StreamOffset<byte[]>... streams) {
        return toPromise(commands.xread(streams));
    }

    @Override
    public Promise<BElement, Exception> flushdb() {
        return toPromise(commands.flushdb());
    }

    @Override
    public Promise<BElement, Exception> zincrby(byte[] key, double amount, byte[] member) {
        return toPromise(commands.zincrby(key, amount, member));
    }

    @Override
    public Promise<BElement, Exception> decr(byte[] key) {
        return toPromise(commands.decr(key));
    }

    @Override
    public Promise<BElement, Exception> sscan(byte[] key) {
        return toPromise(commands.sscan(key));
    }

    @Override
    public Promise<BElement, Exception> clusterKeyslot(byte[] key) {
        return toPromise(commands.clusterKeyslot(key));
    }

    @Override
    public Promise<BElement, Exception> flushdbAsync() {
        return toPromise(commands.flushdbAsync());
    }

    @Override
    public Promise<BElement, Exception> pttl(byte[] key) {
        return toPromise(commands.pttl(key));
    }

    @Override
    public Promise<BElement, Exception> xread(XReadArgs args, StreamOffset<byte[]>... streams) {
        return toPromise(commands.xread(args, streams));
    }

    @Override
    public Promise<BElement, Exception> info() {
        return toPromise(commands.info());
    }

    @Override
    public Promise<BElement, Exception> hscan(KeyValueStreamingChannel<byte[], byte[]> channel, byte[] key, ScanCursor scanCursor) {
        return toPromise(commands.hscan(channel, key, scanCursor));
    }

    @Override
    public Promise<BElement, Exception> sscan(byte[] key, ScanArgs scanArgs) {
        return toPromise(commands.sscan(key, scanArgs));
    }

    @Override
    public Promise<BElement, Exception> decrby(byte[] key, long amount) {
        return toPromise(commands.decrby(key, amount));
    }

    @Override
    public Promise<BElement, Exception> randomkey() {
        return toPromise(commands.randomkey());
    }

    @Override
    public Promise<BElement, Exception> info(String section) {
        return toPromise(commands.info(section));
    }

    @Override
    public Promise<BElement, Exception> zinterstore(byte[] destination, String aggregate, List<Double> weights, byte[]... keys) {
        if (aggregate != null) {
            ZStoreArgs args = buildZStoreArgs(aggregate, weights);
            return toPromise(commands.zinterstore(destination, args, keys));
        }
        return toPromise(commands.zinterstore(destination, keys));
    }

    @Override
    public Promise<BElement, Exception> sscan(byte[] key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return toPromise(commands.sscan(key, scanCursor, scanArgs));
    }

    @Override
    public Promise<BElement, Exception> xreadgroup(Consumer<byte[]> consumer, StreamOffset<byte[]>... streams) {
        return toPromise(commands.xreadgroup(consumer, streams));
    }

    @Override
    public Promise<BElement, Exception> get(byte[] key) {
        return toPromise(commands.get(key));
    }

    @Override
    public Promise<BElement, Exception> clusterSaveconfig() {
        return toPromise(commands.clusterSaveconfig());
    }

    @Override
    public Promise<BElement, Exception> rename(byte[] key, byte[] newKey) {
        return toPromise(commands.rename(key, newKey));
    }

    @Override
    public Promise<BElement, Exception> lastsave() {
        return toPromise(commands.lastsave());
    }

    @Override
    public Promise<BElement, Exception> hset(byte[] key, byte[] field, byte[] value) {
        return toPromise(commands.hset(key, field, value));
    }

    @Override
    public Promise<BElement, Exception> getbit(byte[] key, long offset) {
        return toPromise(commands.getbit(key, offset));
    }

    @Override
    public Promise<BElement, Exception> renamenx(byte[] key, byte[] newKey) {
        return toPromise(commands.renamenx(key, newKey));
    }

    @Override
    public Promise<BElement, Exception> clusterSetConfigEpoch(long configEpoch) {
        return toPromise(commands.clusterSetConfigEpoch(configEpoch));
    }

    @Override
    public Promise<BElement, Exception> save() {
        return toPromise(commands.save());
    }

    @Override
    public Promise<BElement, Exception> sscan(byte[] key, ScanCursor scanCursor) {
        return toPromise(commands.sscan(key, scanCursor));
    }

    @Override
    public Promise<BElement, Exception> xreadgroup(Consumer<byte[]> consumer, XReadArgs args, StreamOffset<byte[]>... streams) {
        return toPromise(commands.xreadgroup(consumer, args, streams));
    }

    public void shutdown(boolean save) {
        commands.shutdown(save);
    }

    @Override
    public Promise<BElement, Exception> getrange(byte[] key, long start, long end) {
        return toPromise(commands.getrange(key, start, end));
    }

    @Override
    public Promise<BElement, Exception> restore(byte[] key, long ttl, byte[] value) {
        return toPromise(commands.restore(key, ttl, value));
    }

    @Override
    public Promise<BElement, Exception> hsetnx(byte[] key, byte[] field, byte[] value) {
        return toPromise(commands.hsetnx(key, field, value));
    }

    @Override
    public Promise<BElement, Exception> sscan(ValueStreamingChannel<byte[]> channel, byte[] key) {
        return toPromise(commands.sscan(channel, key));
    }

    @Override
    public Promise<BElement, Exception> slaveof(String host, int port) {
        return toPromise(commands.slaveof(host, port));
    }

    @Override
    public Promise<BElement, Exception> clusterSlots() {
        return toPromise(commands.clusterSlots());
    }

    @Override
    public Promise<BElement, Exception> getset(byte[] key, byte[] value) {
        return toPromise(commands.getset(key, value));
    }

    @Override
    public Promise<BElement, Exception> xrevrange(byte[] key, Range<String> range) {
        return toPromise(commands.xrevrange(key, range));
    }

    @Override
    public Promise<BElement, Exception> sscan(ValueStreamingChannel<byte[]> channel, byte[] key, ScanArgs scanArgs) {
        return toPromise(commands.sscan(channel, key, scanArgs));
    }

    @Override
    public Promise<BElement, Exception> asking() {
        return toPromise(commands.asking());
    }

    @Override
    public Promise<BElement, Exception> slaveofNoOne() {
        return toPromise(commands.slaveofNoOne());
    }

    @Override
    public Promise<BElement, Exception> restore(byte[] key, byte[] value, RestoreArgs args) {
        return toPromise(commands.restore(key, value, args));
    }

    @Override
    public Promise<BElement, Exception> zlexcount(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper) {
        return toPromise(commands.zlexcount(key, buildRangeBytes(includeLower, lower, upper, includeUpper)));
    }

    @Override
    public Promise<BElement, Exception> slowlogGet() {
        return toPromise(commands.slowlogGet());
    }

    @Override
    public Promise<BElement, Exception> incr(byte[] key) {
        return toPromise(commands.incr(key));
    }

    @Override
    public Promise<BElement, Exception> hstrlen(byte[] key, byte[] field) {
        return toPromise(commands.hstrlen(key, field));
    }

    @Override
    public Promise<BElement, Exception> xrevrange(byte[] key, Range<String> range, Limit limit) {
        return toPromise(commands.xrevrange(key, range, limit));
    }

    @Override
    public Promise<BElement, Exception> slowlogGet(int count) {
        return toPromise(commands.slowlogGet(count));
    }

    @Override
    public Promise<BElement, Exception> clusterReplicate(String nodeId) {
        return toPromise(commands.clusterReplicate(nodeId));
    }

    @Override
    public Promise<BElement, Exception> sscan(ValueStreamingChannel<byte[]> channel, byte[] key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return toPromise(commands.sscan(channel, key, scanCursor, scanArgs));
    }

    @Override
    public Promise<BElement, Exception> incrby(byte[] key, long amount) {
        return toPromise(commands.incrby(key, amount));
    }

    @Override
    public Promise<BElement, Exception> zpopmin(byte[] key) {
        return toPromise(commands.zpopmin(key));
    }

    @Override
    public Promise<BElement, Exception> sort(byte[] key) {
        return toPromise(commands.sort(key));
    }

    @Override
    public Promise<BElement, Exception> slowlogLen() {
        return toPromise(commands.slowlogLen());
    }

    @Override
    public Promise<BElement, Exception> clusterFailover(boolean force) {
        return toPromise(commands.clusterFailover(force));
    }

    @Override
    public Promise<BElement, Exception> hvals(byte[] key) {
        return toPromise(commands.hvals(key));
    }

    @Override
    public Promise<BElement, Exception> slowlogReset() {
        return toPromise(commands.slowlogReset());
    }

    @Override
    public Promise<BElement, Exception> incrbyfloat(byte[] key, double amount) {
        return toPromise(commands.incrbyfloat(key, amount));
    }

    @Override
    public Promise<BElement, Exception> sort(ValueStreamingChannel<byte[]> channel, byte[] key) {
        return toPromise(commands.sort(channel, key));
    }

    @Override
    public Promise<BElement, Exception> xtrim(byte[] key, long count) {
        return toPromise(commands.xtrim(key, count));
    }

    @Override
    public Promise<BElement, Exception> zpopmin(byte[] key, long count) {
        return toPromise(commands.zpopmin(key, count));
    }

    @Override
    public Promise<BElement, Exception> sscan(ValueStreamingChannel<byte[]> channel, byte[] key, ScanCursor scanCursor) {
        return toPromise(commands.sscan(channel, key, scanCursor));
    }

    @Override
    public Promise<BElement, Exception> hvals(ValueStreamingChannel<byte[]> channel, byte[] key) {
        return toPromise(commands.hvals(channel, key));
    }

    @Override
    public Promise<BElement, Exception> time() {
        return toPromise(commands.time());
    }

    @Override
    public Promise<BElement, Exception> clusterReset(boolean hard) {
        return toPromise(commands.clusterReset(hard));
    }

    @Override
    public Promise<BElement, Exception> xtrim(byte[] key, boolean approximateTrimming, long count) {
        return toPromise(commands.xtrim(key, approximateTrimming, count));
    }

    @Override
    public Promise<BElement, Exception> sort(byte[] key, SortArgs sortArgs) {
        return toPromise(commands.sort(key, sortArgs));
    }

    @Override
    public Promise<BElement, Exception> zpopmax(byte[] key) {
        return toPromise(commands.zpopmax(key));
    }

    @Override
    public Promise<BElement, Exception> mget(KeyValueStreamingChannel<byte[], byte[]> channel, byte[]... keys) {
        return toPromise(commands.mget(channel, keys));
    }

    @Override
    public Promise<BElement, Exception> sort(ValueStreamingChannel<byte[]> channel, byte[] key, SortArgs sortArgs) {
        return toPromise(commands.sort(channel, key, sortArgs));
    }

    @Override
    public Promise<BElement, Exception> zpopmax(byte[] key, long count) {
        return toPromise(commands.zpopmax(key, count));
    }

    @Override
    public Promise<BElement, Exception> sortStore(byte[] key, SortArgs sortArgs, byte[] destination) {
        return toPromise(commands.sortStore(key, sortArgs, destination));
    }

    @Override
    public Promise<BElement, Exception> clusterFlushslots() {
        return toPromise(commands.clusterFlushslots());
    }

    @Override
    public Promise<BElement, Exception> zrange(byte[] key, long start, long stop) {
        return toPromise(commands.zrange(key, start, stop));
    }

    @Override
    public Promise<BElement, Exception> touch(byte[]... keys) {
        return toPromise(commands.touch(keys));
    }

    @Override
    public Promise<BElement, Exception> set(byte[] key, byte[] value) {
        return toPromise(commands.set(key, value));
    }

    @Override
    public Promise<BElement, Exception> zrange(java.util.function.Consumer<byte[]> channel, byte[] key, long start, long stop) {
        return toPromise(commands.zrange(bytes -> channel.accept(bytes), key, start, stop));
    }

    @Override
    public Promise<BElement, Exception> ttl(byte[] key) {
        return toPromise(commands.ttl(key));
    }

    @Override
    public Promise<BElement, Exception> del(byte[]... keys) {
        return toPromise(commands.del(keys));
    }

    @Override
    public Promise<BElement, Exception> set(byte[] key, byte[] value, SetArgs setArgs) {
        return toPromise(commands.set(key, value, setArgs));
    }

    @Override
    public Promise<BElement, Exception> type(byte[] key) {
        return toPromise(commands.type(key));
    }

    @Override
    public Promise<BElement, Exception> zrangeWithScores(byte[] key, long start, long stop) {
        return toPromise(commands.zrangeWithScores(key, start, stop));
    }

    @Override
    public Promise<BElement, Exception> mget(byte[]... keys) {
        return toPromise(commands.mget(keys));
    }

    @Override
    public Promise<BElement, Exception> setbit(byte[] key, long offset, int value) {
        return toPromise(commands.setbit(key, offset, value));
    }

    @Override
    public Promise<BElement, Exception> scan() {
        return toPromise(commands.scan());
    }

    @Override
    public Promise<BElement, Exception> scan(ScanArgs scanArgs) {
        return toPromise(commands.scan(scanArgs));
    }

    @Override
    public Promise<BElement, Exception> zrangeWithScores(ScoredValueStreamingChannel<byte[]> channel, byte[] key, long start, long stop) {
        return toPromise(commands.zrangeWithScores(channel, key, start, stop));
    }

    @Override
    public Promise<BElement, Exception> mset(Map<byte[], byte[]> map) {
        return toPromise(commands.mset(map));
    }

    @Override
    public Promise<BElement, Exception> setex(byte[] key, long seconds, byte[] value) {
        return toPromise(commands.setex(key, seconds, value));
    }

    @Override
    public Promise<BElement, Exception> scan(ScanCursor scanCursor, ScanArgs scanArgs) {
        return toPromise(commands.scan(scanCursor, scanArgs));
    }

    @Override
    public Promise<BElement, Exception> msetnx(Map<byte[], byte[]> map) {
        return toPromise(commands.msetnx(map));
    }

    @Override
    public Promise<BElement, Exception> psetex(byte[] key, long milliseconds, byte[] value) {
        return toPromise(commands.psetex(key, milliseconds, value));
    }

    @Override
    public Promise<BElement, Exception> scan(ScanCursor scanCursor) {
        return toPromise(commands.scan(scanCursor));
    }

    @Override
    public Promise<BElement, Exception> setnx(byte[] key, byte[] value) {
        return toPromise(commands.setnx(key, value));
    }

    @Override
    public Promise<BElement, Exception> scan(KeyStreamingChannel<byte[]> channel) {
        return toPromise(commands.scan(channel));
    }

    @Override
    public Promise<BElement, Exception> zrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper, Long offset,
            Long count) {
        Range<byte[]> range = buildRangeBytes(includeLower, lower, upper, includeUpper);
        if (offset != null && count != null) {
            Limit limit = Limit.create(offset, count);
            return toPromise(commands.zrangebylex(key, range, limit));
        }
        return toPromise(commands.zrangebylex(key, range));
    }

    @Override
    public Promise<BElement, Exception> scan(KeyStreamingChannel<byte[]> channel, ScanArgs scanArgs) {
        return toPromise(commands.scan(channel, scanArgs));
    }

    @Override
    public Promise<BElement, Exception> setrange(byte[] key, long offset, byte[] value) {
        return toPromise(commands.setrange(key, offset, value));
    }

    @Override
    public Promise<BElement, Exception> scan(KeyStreamingChannel<byte[]> channel, ScanCursor scanCursor, ScanArgs scanArgs) {
        return toPromise(commands.scan(channel, scanCursor, scanArgs));
    }

    @Override
    public Promise<BElement, Exception> strlen(byte[] key) {
        return toPromise(commands.strlen(key));
    }

    @Override
    public Promise<BElement, Exception> scan(KeyStreamingChannel<byte[]> channel, ScanCursor scanCursor) {
        return toPromise(commands.scan(channel, scanCursor));
    }

    @Override
    public Promise<BElement, Exception> zrangebyscore(java.util.function.Consumer<byte[]> channel, byte[] key, boolean includeLower, long lower, long upper,
            boolean includeUpper, Long offset, Long count) {
        Range<Long> range = buildRangeLong(includeLower, lower, upper, includeUpper);
        if (offset != null && count != null) {
            Limit limit = Limit.create(offset, count);
            if (channel == null) {
                return toPromise(commands.zrangebyscore(key, range, limit));
            } else {
                return toPromise(commands.zrangebyscore(bytes -> channel.accept(bytes), key, range, limit));
            }
        }
        if (channel == null) {
            return toPromise(commands.zrangebyscore(key, range));
        }
        return toPromise(commands.zrangebyscore(bytes -> channel.accept(bytes), key, range));
    }

    @Override
    public Promise<BElement, Exception> zrangebyscoreWithScores(byte[] key, Range<? extends Number> range) {
        return toPromise(commands.zrangebyscoreWithScores(key, range));
    }

    @Override
    public Promise<BElement, Exception> zrangebyscoreWithScores(byte[] key, Range<? extends Number> range, Limit limit) {
        return toPromise(commands.zrangebyscoreWithScores(key, range, limit));
    }

    @Override
    public Promise<BElement, Exception> zrangebyscoreWithScores(ScoredValueStreamingChannel<byte[]> channel, byte[] key, Range<? extends Number> range) {
        return toPromise(commands.zrangebyscoreWithScores(channel, key, range));
    }

    @Override
    public Promise<BElement, Exception> zrangebyscoreWithScores(ScoredValueStreamingChannel<byte[]> channel, byte[] key, Range<? extends Number> range,
            Limit limit) {
        return toPromise(commands.zrangebyscoreWithScores(channel, key, range, limit));
    }

    @Override
    public Promise<BElement, Exception> zrank(byte[] key, byte[] member) {
        return toPromise(commands.zrank(key, member));
    }

    @Override
    public Promise<BElement, Exception> zrem(byte[] key, byte[]... members) {
        return toPromise(commands.zrem(key, members));
    }

    @Override
    public Promise<BElement, Exception> zremrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper) {
        return toPromise(commands.zremrangebylex(key, buildRangeBytes(includeLower, lower, upper, includeUpper)));
    }

    @Override
    public Promise<BElement, Exception> zremrangebyrank(byte[] key, long start, long stop) {
        return toPromise(commands.zremrangebyrank(key, start, stop));
    }

    @Override
    public Promise<BElement, Exception> zremrangebyscore(byte[] key, boolean includeLower, long lower, long upper, boolean includeUpper) {
        return toPromise(commands.zremrangebyscore(key, buildRangeLong(includeLower, lower, upper, includeUpper)));
    }

    @Override
    public Promise<BElement, Exception> zrevrange(java.util.function.Consumer<byte[]> channel, byte[] key, long start, long stop) {
        if (channel == null) {
            return toPromise(commands.zrevrange(key, start, stop));
        }
        return toPromise(commands.zrevrange(bytes -> channel.accept(bytes), key, start, stop));
    }

    @Override
    public Promise<BElement, Exception> zrevrangeWithScores(byte[] key, long start, long stop) {
        return toPromise(commands.zrevrangeWithScores(key, start, stop));
    }

    @Override
    public Promise<BElement, Exception> zrevrangebylex(byte[] key, boolean includeLower, byte[] lower, byte[] upper, boolean includeUpper, Long offset,
            Long count) {
        Range<byte[]> range = buildRangeBytes(includeLower, lower, upper, includeUpper);
        if (offset != null && count != null) {
            Limit limit = Limit.create(offset, count);
            return toPromise(commands.zrangebylex(key, range, limit));
        }
        return toPromise(commands.zrevrangebylex(key, range));
    }

    @Override
    public Promise<BElement, Exception> zrevrangebyscore(java.util.function.Consumer<byte[]> channel, byte[] key, boolean includeLower, long lower, long upper,
            boolean includeUpper, Long offset, Long count) {
        Range<Long> range = buildRangeLong(includeLower, lower, upper, includeUpper);
        if (offset != null && count != null) {
            Limit limit = Limit.create(offset, count);
            if (channel == null) {
                return toPromise(commands.zrevrangebyscore(key, range, limit));
            } else {
                return toPromise(commands.zrevrangebyscore(bytes -> channel.accept(bytes), key, range, limit));
            }
        }
        if (channel != null) {
            return toPromise(commands.zrevrangebyscore(bytes -> channel.accept(bytes), key, range));
        }
        return toPromise(commands.zrevrangebyscore(key, range));
    }

    @Override
    public Promise<BElement, Exception> zrevrank(byte[] key, byte[] member) {
        return toPromise(commands.zrevrank(key, member));
    }

    @Override
    public Promise<BElement, Exception> zscan(@NonNull BiConsumer<Double, byte[]> consumer, byte[] key, String cursor, String match, Long limit) {

        ScanCursor scanCursor = cursor == null ? null : new ScanCursor();
        ScanArgs scanArgs = (match != null || limit != null) ? ScanArgs.Builder.limit(limit).match(match) : null;

        final RedisFuture<StreamScanCursor> future;

        ScoredValueStreamingChannel<byte[]> channel = scoredValue -> consumer.accept(scoredValue.getScore(), scoredValue.getValue());
        if (cursor != null) {
            scanCursor.setCursor(cursor);
            if (scanArgs == null) {
                future = commands.zscan(channel, key, scanCursor);
            } else {
                future = commands.zscan(channel, key, scanCursor, scanArgs);
            }
        } else {
            if (scanArgs == null) {
                future = commands.zscan(channel, key);
            } else {
                future = commands.zscan(channel, key, scanArgs);
            }
        }

        return toPromise(future).filterDone(ref -> {
            StreamScanCursor streamScanCursor = ref.asReference().getReference();
            return BObject.ofSequence("count", streamScanCursor.getCount(), "cursor", streamScanCursor.getCursor(), "finished", streamScanCursor.isFinished());
        });
    }

    @Override
    public Promise<BElement, Exception> zscore(byte[] key, byte[] member) {
        return toPromise(commands.zscore(key, member));
    }

    @Override
    public Promise<BElement, Exception> zunionstore(byte[] destination, String aggregate, List<Double> weights, byte[]... keys) {
        ZStoreArgs storeArgs = null;
        if (aggregate != null) {
            storeArgs = buildZStoreArgs(aggregate, weights);
        }
        if (storeArgs != null) {
            return toPromise(commands.zunionstore(destination, storeArgs, keys));
        }
        return toPromise(commands.zunionstore(destination, keys));
    }

    @Override
    public Promise<BElement, Exception> zadd(byte[] key, boolean xx, boolean nx, boolean ch, Object... scoresAndValues) {
        ZAddArgs args = new ZAddArgs();
        if (xx)
            args.xx();
        if (nx)
            args.nx();
        if (ch)
            args.ch();
        return toPromise(commands.zadd(key, args, scoresAndValues));
    }
}
