package io.arex.inst.redis.common.lettuce.wrapper;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.redis.common.RedisKeyUtil;
import io.arex.inst.redis.common.lettuce.RedisCommandBuilderImpl;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.lettuce.core.AbstractRedisAsyncCommands;
import io.lettuce.core.GetExArgs;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.SetArgs;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.internal.LettuceAssert;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingChannel;
import io.lettuce.core.protocol.AsyncCommand;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.RedisCommand;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * RedisAsyncCommandsImplWrapper
 */
public class RedisCommandWrapper<K, V> {

    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String INDEX = "index";
    private static final String TIMESTAMP_LOG = "Timestamp must not be null";
    private static final String TIMEOUT_LOG = "Timeout must not be null";

    private final RedisCommandBuilderImpl<K, V> commandBuilder;

    public RedisCommandWrapper(RedisCodec<K, V> codec) {
        this.commandBuilder = new RedisCommandBuilderImpl<>(codec);
    }

    public RedisCommandBuilderImpl<K, V> getCommandBuilder() {
        return commandBuilder;
    }

    public RedisFuture<Long> append(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, V value) {
        RedisCommand<K, V, Long> cmd = commandBuilder.append(key, value);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> decr(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        RedisCommand<K, V, Long> cmd = commandBuilder.decr(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> decrby(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long amount) {
        RedisCommand<K, V, Long> cmd = commandBuilder.decrby(key, amount);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> del(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K... keys) {
        Command<K, V, Long> cmd = commandBuilder.del(keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<Long> del(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.del(keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<Long> exists(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K... keys) {
        Command<K, V, Long> cmd = commandBuilder.exists(keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<Long> exists(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.exists(keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<Boolean> expire(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long seconds) {
        Command<K, V, Boolean> cmd = commandBuilder.expire(key, seconds);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Boolean> expire(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, Duration seconds) {
        LettuceAssert.notNull(seconds, TIMEOUT_LOG);
        return expire(abstractRedisAsyncCommands, redisUri, key, seconds.toMillis() / 1000);
    }


    public RedisFuture<Boolean> expireat(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long timestamp) {
        Command<K, V, Boolean> cmd = commandBuilder.expireat(key, timestamp);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Boolean> expireat(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, Date timestamp) {
        LettuceAssert.notNull(timestamp, TIMESTAMP_LOG);
        return expireat(abstractRedisAsyncCommands, redisUri, key, timestamp.getTime() / 1000);
    }


    public RedisFuture<Boolean> expireat(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, Instant timestamp) {
        LettuceAssert.notNull(timestamp, TIMESTAMP_LOG);
        return expireat(abstractRedisAsyncCommands, redisUri, key, timestamp.toEpochMilli() / 1000);
    }


    public RedisFuture<V> get(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        RedisCommand<K, V, V> cmd = commandBuilder.get(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> getbit(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long offset) {
        Command<K, V, Long> cmd = commandBuilder.getbit(key, offset);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }

    public RedisFuture<V> getdel(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, V> cmd = commandBuilder.getdel(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }

    public RedisFuture<V> getex(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, GetExArgs args) {
        Command<K, V, V> cmd = commandBuilder.getex(key, args);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<V> getrange(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long start, long end) {
        Command<K, V, V> cmd = commandBuilder.getrange(key, start, end);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<V> getset(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, V value) {
        Command<K, V, V> cmd = commandBuilder.getset(key, value);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> hdel(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, K... fields) {
        Command<K, V, Long> cmd = commandBuilder.hdel(key, fields);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key), RedisKeyUtil.generate(fields));
    }


    public RedisFuture<Boolean> hexists(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, K field) {
        Command<K, V, Boolean> cmd = commandBuilder.hexists(key, field);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key), RedisKeyUtil.generate(field));
    }


    public RedisFuture<V> hget(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, K field) {
        Command<K, V, V> cmd = commandBuilder.hget(key, field);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key), RedisKeyUtil.generate(field));
    }


    public RedisFuture<Map<K, V>> hgetall(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, Map<K, V>> cmd = commandBuilder.hgetall(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> hgetall(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        KeyValueStreamingChannel<K, V> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hgetall(channel, key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> hincrby(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, K field, long amount) {
        Command<K, V, Long> cmd = commandBuilder.hincrby(key, field, amount);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key), RedisKeyUtil.generate(field));
    }


    public RedisFuture<Double> hincrbyfloat(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands,
        String redisUri, K key, K field, double amount) {
        Command<K, V, Double> cmd = commandBuilder.hincrbyfloat(key, field, amount);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key), RedisKeyUtil.generate(field));
    }


    public RedisFuture<List<K>> hkeys(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, List<K>> cmd = commandBuilder.hkeys(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> hkeys(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        KeyStreamingChannel<K> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hkeys(channel, key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> hlen(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, Long> cmd = commandBuilder.hlen(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<List<KeyValue<K, V>>> hmget(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands,
        String redisUri, K key, K... fields) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.hmgetKeyValue(key, fields);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key), RedisKeyUtil.generate(fields));
    }


    public RedisFuture<Long> hmget(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        KeyValueStreamingChannel<K, V> channel, K key, K... fields) {
        Command<K, V, Long> cmd = commandBuilder.hmget(channel, key, fields);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key), RedisKeyUtil.generate(fields));
    }


    public RedisFuture<String> hmset(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, Map<K, V> map) {
        Command<K, V, String> cmd = commandBuilder.hmset(key, map);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key), RedisKeyUtil.generate(map));
    }


    public RedisFuture<Boolean> hset(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, K field, V value) {
        Command<K, V, Boolean> cmd = commandBuilder.hset(key, field, value);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key), RedisKeyUtil.generate(field));
    }


    public RedisFuture<Long> hset(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, Map<K, V> map) {
        Command<K, V, Long> cmd = commandBuilder.hset(key, map);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key), RedisKeyUtil.generate(map));
    }


    public RedisFuture<Boolean> hsetnx(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, K field, V value) {
        Command<K, V, Boolean> cmd = commandBuilder.hsetnx(key, field, value);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key), RedisKeyUtil.generate(field));
    }


    public RedisFuture<List<V>> hvals(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, List<V>> cmd = commandBuilder.hvals(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> hvals(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        ValueStreamingChannel<V> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hvals(channel, key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> incr(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, Long> cmd = commandBuilder.incr(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> incrby(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long amount) {
        Command<K, V, Long> cmd = commandBuilder.incrby(key, amount);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Double> incrbyfloat(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, double amount) {
        Command<K, V, Double> cmd = commandBuilder.incrbyfloat(key, amount);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<List<K>> keys(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K pattern) {
        Command<K, V, List<K>> cmd = commandBuilder.keys(pattern);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(pattern));
    }


    public RedisFuture<Long> keys(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        KeyStreamingChannel<K> channel, K pattern) {
        Command<K, V, Long> cmd = commandBuilder.keys(channel, pattern);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(pattern));
    }


    public RedisFuture<V> lindex(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long index) {
        Command<K, V, V> cmd = commandBuilder.lindex(key, index);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key),
            RedisKeyUtil.generate(INDEX, RedisKeyUtil.generate(index)));
    }


    public RedisFuture<Long> llen(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, Long> cmd = commandBuilder.llen(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<V> lpop(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, V> cmd = commandBuilder.lpop(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<List<V>> lpop(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long count) {
        Command<K, V, List<V>> cmd = commandBuilder.lpop(key, count);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<List<V>> lrange(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long start, long stop) {
        Command<K, V, List<V>> cmd = commandBuilder.lrange(key, start, stop);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key),
            RedisKeyUtil.generate(START, RedisKeyUtil.generate(start), STOP, RedisKeyUtil.generate(stop)));
    }


    public RedisFuture<Long> lrange(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        ValueStreamingChannel<V> channel, K key, long start, long stop) {
        Command<K, V, Long> cmd = commandBuilder.lrange(channel, key, start, stop);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key),
            RedisKeyUtil.generate(START, RedisKeyUtil.generate(start), STOP, RedisKeyUtil.generate(stop)));
    }


    public RedisFuture<String> lset(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long index, V value) {
        Command<K, V, String> cmd = commandBuilder.lset(key, index, value);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key),
            RedisKeyUtil.generate(INDEX, RedisKeyUtil.generate(index)));
    }


    public RedisFuture<String> ltrim(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long start, long stop) {
        Command<K, V, String> cmd = commandBuilder.ltrim(key, start, stop);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key),
            RedisKeyUtil.generate(START, RedisKeyUtil.generate(start), STOP, RedisKeyUtil.generate(stop)));
    }


    public RedisFuture<List<KeyValue<K, V>>> mget(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands,
        String redisUri, K... keys) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.mgetKeyValue(keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<List<KeyValue<K, V>>> mget(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands,
        String redisUri, Iterable<K> keys) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.mgetKeyValue(keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<Long> mget(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        KeyValueStreamingChannel<K, V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.mget(channel, keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<Long> mget(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        KeyValueStreamingChannel<K, V> channel, Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.mget(channel, keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<String> mset(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        Map<K, V> map) {
        Command<K, V, String> cmd = commandBuilder.mset(map);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(map));
    }


    public RedisFuture<Boolean> msetnx(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        Map<K, V> map) {
        Command<K, V, Boolean> cmd = commandBuilder.msetnx(map);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(map));
    }


    public RedisFuture<Boolean> persist(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, Boolean> cmd = commandBuilder.persist(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Boolean> pexpire(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long milliseconds) {
        Command<K, V, Boolean> cmd = commandBuilder.pexpire(key, milliseconds);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Boolean> pexpire(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, Duration milliseconds) {
        LettuceAssert.notNull(milliseconds, TIMEOUT_LOG);
        return pexpire(abstractRedisAsyncCommands, redisUri, key, milliseconds.toMillis());
    }


    public RedisFuture<Boolean> pexpireat(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, Date timestamp) {
        LettuceAssert.notNull(timestamp, TIMESTAMP_LOG);
        return pexpireat(abstractRedisAsyncCommands, redisUri, key, timestamp.getTime());
    }


    public RedisFuture<Boolean> pexpireat(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, Instant timestamp) {
        LettuceAssert.notNull(timestamp, TIMESTAMP_LOG);
        return pexpireat(abstractRedisAsyncCommands, redisUri, key, timestamp.toEpochMilli());
    }


    public RedisFuture<Boolean> pexpireat(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long timestamp) {
        Command<K, V, Boolean> cmd = commandBuilder.pexpireat(key, timestamp);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<String> psetex(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long milliseconds, V value) {
        Command<K, V, String> cmd = commandBuilder.psetex(key, milliseconds, value);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> pttl(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, Long> cmd = commandBuilder.pttl(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<String> rename(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, K newKey) {
        Command<K, V, String> cmd = commandBuilder.rename(key, newKey);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key, newKey));
    }


    public RedisFuture<Boolean> renamenx(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, K newKey) {
        Command<K, V, Boolean> cmd = commandBuilder.renamenx(key, newKey);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key, newKey));
    }


    public RedisFuture<V> rpop(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, V> cmd = commandBuilder.rpop(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<List<V>> rpop(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long count) {
        Command<K, V, List<V>> cmd = commandBuilder.rpop(key, count);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<V> rpoplpush(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K source, K destination) {
        Command<K, V, V> cmd = commandBuilder.rpoplpush(source, destination);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(source), RedisKeyUtil.generate(destination));
    }


    public RedisFuture<Long> scard(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, Long> cmd = commandBuilder.scard(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Set<V>> sdiff(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sdiff(keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<Long> sdiff(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sdiff(channel, keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<String> set(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, V value) {
        Command<K, V, String> cmd = commandBuilder.set(key, value);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<String> set(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, V value, SetArgs setArgs) {
        Command<K, V, String> cmd = commandBuilder.set(key, value, setArgs);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<V> setGet(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, V value) {
        Command<K, V, V> cmd = commandBuilder.setGet(key, value);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<V> setGet(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, V value, SetArgs setArgs) {
        Command<K, V, V> cmd = commandBuilder.setGet(key, value, setArgs);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<String> setex(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long seconds, V value) {
        Command<K, V, String> cmd = commandBuilder.setex(key, seconds, value);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Boolean> setnx(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, V value) {
        Command<K, V, Boolean> cmd = commandBuilder.setnx(key, value);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> setrange(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key, long offset, V value) {
        Command<K, V, Long> cmd = commandBuilder.setrange(key, offset, value);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key),
            RedisKeyUtil.generate("offset", RedisKeyUtil.generate(offset)));
    }


    public RedisFuture<Set<V>> sinter(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sinter(keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<Long> sinter(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sinter(channel, keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<V> spop(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, String redisUri,
        K key) {
        Command<K, V, V> cmd = commandBuilder.spop(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Set<V>> spop(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, final String redisUri,
        K key, long count) {
        Command<K, V, Set<V>> cmd = commandBuilder.spop(key, count);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<V> srandmember(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands,
        final String redisUri, K key) {
        Command<K, V, V> cmd = commandBuilder.srandmember(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<List<V>> srandmember(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands,
        final String redisUri, K key, long count) {
        Command<K, V, List<V>> cmd = commandBuilder.srandmember(key, count);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> srandmember(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands,
        final String redisUri, ValueStreamingChannel<V> channel, K key,
        long count) {
        Command<K, V, Long> cmd = commandBuilder.srandmember(channel, key, count);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> strlen(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, final String redisUri,
        K key) {
        Command<K, V, Long> cmd = commandBuilder.strlen(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Set<V>> sunion(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands,
        final String redisUri, K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sunion(keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<Long> sunion(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, final String redisUri,
        ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sunion(channel, keys);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(keys));
    }


    public RedisFuture<Long> ttl(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, final String redisUri,
        K key) {
        Command<K, V, Long> cmd = commandBuilder.ttl(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<String> type(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, final String redisUri,
        K key) {
        Command<K, V, String> cmd = commandBuilder.type(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }


    public RedisFuture<Long> zcard(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands, final String redisUri,
        K key) {
        Command<K, V, Long> cmd = commandBuilder.zcard(key);
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, RedisKeyUtil.generate(key));
    }

    private <T> RedisFuture<T> dispatch(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands,
        final String redisUri, RedisCommand<K, V, T> cmd, String key) {
        return dispatch(abstractRedisAsyncCommands, redisUri, cmd, key, null);
    }

    private <T> RedisFuture<T> dispatch(AbstractRedisAsyncCommands<K, V> abstractRedisAsyncCommands,
        final String redisUri, RedisCommand<K, V, T> cmd, String key, String field) {
        return dispatch(() -> abstractRedisAsyncCommands.dispatch(cmd), cmd, key, redisUri, field);
    }

    public <T> RedisFuture<T> dispatch(Supplier<RedisFuture<T>> supplier, RedisCommand<K, V, T> cmd, String key, String redisUri) {
        return dispatch(supplier, cmd, key, redisUri, null);
    }

    public <T> RedisFuture<T> dispatch(Supplier<RedisFuture<T>> supplier, RedisCommand<K, V, T> cmd, String key, String redisUri, String field) {
        if (ContextManager.needRecord()) {
            RepeatedCollectManager.enter();
        }

        if (ContextManager.needReplay()) {
            AsyncCommand<K, V, T> asyncCommand = new AsyncCommand<>(cmd);
            RedisExtractor extractor = new RedisExtractor(redisUri, cmd.getType().name(), key, field);
            MockResult mockResult = extractor.replay();
            if (mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    asyncCommand.completeExceptionally(mockResult.getThrowable());
                } else {
                    asyncCommand.complete((T) mockResult.getResult());
                }
                return asyncCommand;
            }
        }

        RedisFuture<T> resultFuture = supplier.get();

        if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate("redis repeat record")) {
            try (TraceTransmitter traceTransmitter = TraceTransmitter.create()) {
                resultFuture.whenComplete((v, throwable) -> {
                    RedisExtractor extractor = new RedisExtractor(redisUri, cmd.getType().name(), key, field);
                    traceTransmitter.transmit();
                    if (throwable != null) {
                        extractor.record(throwable);
                    } else {
                        extractor.record(v);
                    }
                });
            }
            return resultFuture;
        }

        return resultFuture;
    }
}
