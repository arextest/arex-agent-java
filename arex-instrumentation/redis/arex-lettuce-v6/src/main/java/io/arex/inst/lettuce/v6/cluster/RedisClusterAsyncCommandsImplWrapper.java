package io.arex.inst.lettuce.v6.cluster;

import io.arex.inst.redis.common.RedisConnectionManager;
import io.arex.inst.redis.common.RedisKeyUtil;
import io.arex.inst.redis.common.lettuce.wrapper.RedisCommandWrapper;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.SetArgs;
import io.lettuce.core.cluster.RedisAdvancedClusterAsyncCommandsImpl;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingChannel;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RedisClusterAsyncCommandsImplWrapper
 */
public class RedisClusterAsyncCommandsImplWrapper<K, V> extends RedisAdvancedClusterAsyncCommandsImpl<K, V> {

    private String redisUri;
    private RedisCommandWrapper<K, V> redisCommandWrapper;
    public RedisClusterAsyncCommandsImplWrapper(StatefulRedisClusterConnection<K, V> connection,
        RedisCodec<K, V> codec) {
        super(connection, codec);
        this.redisCommandWrapper = new RedisCommandWrapper<>(codec);
    }

    @Override
    public RedisFuture<Long> append(K key, V value) {
        return redisCommandWrapper.append(this, getRedisUri(), key, value);
    }

    @Override
    public RedisFuture<Long> decr(K key) {
        return redisCommandWrapper.decr(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<Long> decrby(K key, long amount) {
        return redisCommandWrapper.decrby(this, getRedisUri(), key, amount);
    }

    @Override
    public RedisFuture<Boolean> expire(K key, long seconds) {
        return redisCommandWrapper.expire(this, getRedisUri(), key, seconds);
    }

    @Override
    public RedisFuture<Boolean> expireat(K key, long timestamp) {
        return redisCommandWrapper.expireat(this, getRedisUri(), key, timestamp);
    }

    @Override
    public RedisFuture<Boolean> expireat(K key, Date timestamp) {
        return redisCommandWrapper.expireat(this, getRedisUri(), key, timestamp);
    }

    @Override
    public RedisFuture<V> get(K key) {
        return redisCommandWrapper.get(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<Long> getbit(K key, long offset) {
        return redisCommandWrapper.getbit(this, getRedisUri(), key, offset);
    }

    @Override
    public RedisFuture<V> getrange(K key, long start, long end) {
        return redisCommandWrapper.getrange(this, getRedisUri(), key, start, end);
    }

    @Override
    public RedisFuture<V> getset(K key, V value) {
        return redisCommandWrapper.getset(this, getRedisUri(), key, value);
    }

    @Override
    public RedisFuture<Long> hdel(K key, K... fields) {
        return redisCommandWrapper.hdel(this, getRedisUri(), key, fields);
    }

    @Override
    public RedisFuture<Boolean> hexists(K key, K field) {
        return redisCommandWrapper.hexists(this, getRedisUri(), key, field);
    }

    @Override
    public RedisFuture<V> hget(K key, K field) {
        return redisCommandWrapper.hget(this, getRedisUri(), key, field);
    }

    @Override
    public RedisFuture<Map<K, V>> hgetall(K key) {
        return redisCommandWrapper.hgetall(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<Long> hgetall(KeyValueStreamingChannel<K, V> channel, K key) {
        return redisCommandWrapper.hgetall(this, getRedisUri(), channel, key);
    }

    @Override
    public RedisFuture<Long> hincrby(K key, K field, long amount) {
        return redisCommandWrapper.hincrby(this, getRedisUri(), key, field, amount);
    }

    @Override
    public RedisFuture<Double> hincrbyfloat(K key, K field, double amount) {
        return redisCommandWrapper.hincrbyfloat(this, getRedisUri(), key, field, amount);
    }

    @Override
    public RedisFuture<List<K>> hkeys(K key) {
        return redisCommandWrapper.hkeys(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<Long> hkeys(KeyStreamingChannel<K> channel, K key) {
        return redisCommandWrapper.hkeys(this, getRedisUri(), channel, key);
    }

    @Override
    public RedisFuture<Long> hlen(K key) {
        return redisCommandWrapper.hlen(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<List<KeyValue<K, V>>> hmget(K key, K... fields) {
        return redisCommandWrapper.hmget(this, getRedisUri(), key, fields);
    }

    @Override
    public RedisFuture<Long> hmget(KeyValueStreamingChannel<K, V> channel, K key, K... fields) {
        return redisCommandWrapper.hmget(this, getRedisUri(), channel, key, fields);
    }

    @Override
    public RedisFuture<String> hmset(K key, Map<K, V> map) {
        return redisCommandWrapper.hmset(this, getRedisUri(), key, map);
    }

    @Override
    public RedisFuture<Boolean> hset(K key, K field, V value) {
        return redisCommandWrapper.hset(this, getRedisUri(), key, field, value);
    }

    @Override
    public RedisFuture<Long> hset(K key, Map<K, V> map) {
        return redisCommandWrapper.hset(this, getRedisUri(), key, map);
    }

    @Override
    public RedisFuture<Boolean> hsetnx(K key, K field, V value) {
        return redisCommandWrapper.hsetnx(this, getRedisUri(), key, field, value);
    }

    @Override
    public RedisFuture<List<V>> hvals(K key) {
        return redisCommandWrapper.hvals(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<Long> hvals(ValueStreamingChannel<V> channel, K key) {
        return redisCommandWrapper.hvals(this, getRedisUri(), channel, key);
    }

    @Override
    public RedisFuture<Long> incr(K key) {
        return redisCommandWrapper.incr(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<Long> incrby(K key, long amount) {
        return redisCommandWrapper.incrby(this, getRedisUri(), key, amount);
    }

    @Override
    public RedisFuture<Double> incrbyfloat(K key, double amount) {
        return redisCommandWrapper.incrbyfloat(this, getRedisUri(), key, amount);
    }

    @Override
    public RedisFuture<V> lindex(K key, long index) {
        return redisCommandWrapper.lindex(this, getRedisUri(), key, index);
    }

    @Override
    public RedisFuture<Long> llen(K key) {
        return redisCommandWrapper.llen(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<V> lpop(K key) {
        return redisCommandWrapper.lpop(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<List<V>> lrange(K key, long start, long stop) {
        return redisCommandWrapper.lrange(this, getRedisUri(), key, start, stop);
    }

    @Override
    public RedisFuture<Long> lrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        return redisCommandWrapper.lrange(this, getRedisUri(), channel, key, start, stop);
    }

    @Override
    public RedisFuture<String> lset(K key, long index, V value) {
        return redisCommandWrapper.lset(this, getRedisUri(), key, index, value);
    }

    @Override
    public RedisFuture<String> ltrim(K key, long start, long stop) {
        return redisCommandWrapper.ltrim(this, getRedisUri(), key, start, stop);
    }

    @Override
    public RedisFuture<Boolean> persist(K key) {
        return redisCommandWrapper.persist(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<Boolean> pexpire(K key, long milliseconds) {
        return redisCommandWrapper.pexpire(this, getRedisUri(), key, milliseconds);

    }

    @Override
    public RedisFuture<Boolean> pexpireat(K key, Date timestamp) {
        return redisCommandWrapper.pexpireat(this, getRedisUri(), key, timestamp);
    }

    @Override
    public RedisFuture<Boolean> pexpireat(K key, long timestamp) {
        return redisCommandWrapper.pexpireat(this, getRedisUri(), key, timestamp);
    }

    @Override
    public RedisFuture<String> psetex(K key, long milliseconds, V value) {
        return redisCommandWrapper.psetex(this, getRedisUri(), key, milliseconds, value);
    }

    @Override
    public RedisFuture<Long> pttl(K key) {
        return redisCommandWrapper.pttl(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<String> rename(K key, K newKey) {
        return redisCommandWrapper.rename(this, getRedisUri(), key, newKey);
    }

    @Override
    public RedisFuture<Boolean> renamenx(K key, K newKey) {
        return redisCommandWrapper.renamenx(this, getRedisUri(), key, newKey);
    }

    @Override
    public RedisFuture<V> rpop(K key) {
        return redisCommandWrapper.rpop(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<List<V>> rpop(K key, long count) {
        return redisCommandWrapper.rpop(this, getRedisUri(), key, count);
    }

    @Override
    public RedisFuture<V> rpoplpush(K source, K destination) {
        return redisCommandWrapper.rpoplpush(this, getRedisUri(), source, destination);
    }

    @Override
    public RedisFuture<Long> scard(K key) {
        return redisCommandWrapper.scard(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<Set<V>> sdiff(K... keys) {
        return redisCommandWrapper.sdiff(this, getRedisUri(), keys);
    }

    @Override
    public RedisFuture<Long> sdiff(ValueStreamingChannel<V> channel, K... keys) {
        return redisCommandWrapper.sdiff(this, getRedisUri(), channel, keys);
    }

    @Override
    public RedisFuture<String> set(K key, V value) {
        return redisCommandWrapper.set(this, getRedisUri(), key, value);
    }

    @Override
    public RedisFuture<String> set(K key, V value, SetArgs setArgs) {
        return redisCommandWrapper.set(this, getRedisUri(), key, value, setArgs);
    }

    @Override
    public RedisFuture<String> setex(K key, long seconds, V value) {
        return redisCommandWrapper.setex(this, getRedisUri(), key, seconds, value);
    }

    @Override
    public RedisFuture<Boolean> setnx(K key, V value) {
        return redisCommandWrapper.setnx(this, getRedisUri(), key, value);
    }

    @Override
    public RedisFuture<Long> setrange(K key, long offset, V value) {
        return redisCommandWrapper.setrange(this, getRedisUri(), key, offset, value);
    }

    @Override
    public RedisFuture<Set<V>> sinter(K... keys) {
        return redisCommandWrapper.sinter(this, getRedisUri(), keys);
    }

    @Override
    public RedisFuture<Long> sinter(ValueStreamingChannel<V> channel, K... keys) {
        return redisCommandWrapper.sinter(this, getRedisUri(), channel, keys);
    }

    @Override
    public RedisFuture<V> spop(K key) {
        return redisCommandWrapper.spop(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<Set<V>> spop(K key, long count) {
        return redisCommandWrapper.spop(this, getRedisUri(), key, count);
    }

    @Override
    public RedisFuture<V> srandmember(K key) {
        return redisCommandWrapper.srandmember(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<List<V>> srandmember(K key, long count) {
        return redisCommandWrapper.srandmember(this, getRedisUri(), key, count);
    }

    @Override
    public RedisFuture<Long> srandmember(ValueStreamingChannel<V> channel, K key, long count) {
        return redisCommandWrapper.srandmember(this, getRedisUri(), channel, key, count);
    }

    @Override
    public RedisFuture<Long> strlen(K key) {
        return redisCommandWrapper.strlen(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<Set<V>> sunion(K... keys) {
        return redisCommandWrapper.sunion(this, getRedisUri(), keys);
    }

    @Override
    public RedisFuture<Long> sunion(ValueStreamingChannel<V> channel, K... keys) {
        return redisCommandWrapper.sunion(this, getRedisUri(), channel, keys);
    }

    @Override
    public RedisFuture<Long> ttl(K key) {
        return redisCommandWrapper.ttl(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<String> type(K key) {
        return redisCommandWrapper.type(this, getRedisUri(), key);
    }

    @Override
    public RedisFuture<Long> zcard(K key) {
        return redisCommandWrapper.zcard(this, getRedisUri(), key);
    }

    // The following methods are special handling in Redis cluster

    @Override
    public RedisFuture<Long> del(Iterable<K> keys) {
        return redisCommandWrapper.dispatch(() -> super.del(keys), redisCommandWrapper.getCommandBuilder().del(keys),
            RedisKeyUtil.generate(keys), getRedisUri());
    }

    @Override
    public RedisFuture<Long> exists(Iterable<K> keys) {
        return redisCommandWrapper.dispatch(() -> super.exists(keys), redisCommandWrapper.getCommandBuilder().exists(keys),
            RedisKeyUtil.generate(keys), getRedisUri());
    }

    @Override
    public RedisFuture<List<K>> keys(K pattern) {
        return redisCommandWrapper.dispatch(() -> super.keys(pattern), redisCommandWrapper.getCommandBuilder().keys(pattern),
            RedisKeyUtil.generate(pattern), getRedisUri());
    }

    @Override
    public RedisFuture<Long> keys(KeyStreamingChannel<K> channel, K pattern) {
        return redisCommandWrapper.dispatch(() -> super.keys(channel, pattern), redisCommandWrapper.getCommandBuilder().keys(channel, pattern),
            RedisKeyUtil.generate(pattern), getRedisUri());
    }

    @Override
    public RedisFuture<List<KeyValue<K, V>>> mget(Iterable<K> keys) {
        return redisCommandWrapper.dispatch(() -> super.mget(keys), redisCommandWrapper.getCommandBuilder().mgetKeyValue(keys),
            RedisKeyUtil.generate(keys), getRedisUri());
    }

    @Override
    public RedisFuture<Long> mget(KeyValueStreamingChannel<K, V> channel, Iterable<K> keys) {
        return redisCommandWrapper.dispatch(() -> super.mget(channel, keys), redisCommandWrapper.getCommandBuilder().mget(channel, keys),
            RedisKeyUtil.generate(keys), getRedisUri());
    }

    @Override
    public RedisFuture<String> mset(Map<K, V> map) {
        return redisCommandWrapper.dispatch(() -> super.mset(map), redisCommandWrapper.getCommandBuilder().mset(map),
            RedisKeyUtil.generate(map), getRedisUri());
    }

    @Override
    public RedisFuture<Boolean> msetnx(Map<K, V> map) {
        return redisCommandWrapper.dispatch(() -> super.msetnx(map), redisCommandWrapper.getCommandBuilder().msetnx(map),
            RedisKeyUtil.generate(map), getRedisUri());
    }

    private String getRedisUri() {
        if (redisUri == null) {
            redisUri = RedisConnectionManager.getRedisUri(this.getStatefulConnection().hashCode());
        }
        return redisUri;
    }
}
