package io.arex.inst.lettuce.v6;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.redis.common.RedisKeyUtil;
import io.lettuce.core.GetExArgs;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisAsyncCommandsImpl;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
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

/**
 * RedisAsyncCommandsImplWrapper
 */
public class RedisAsyncCommandsImplWrapper<K, V> extends RedisAsyncCommandsImpl<K, V> {
    private final RedisCommandBuilderImpl<K, V> commandBuilder;
    private String redisUri;

    public RedisAsyncCommandsImplWrapper(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
        this.commandBuilder = new RedisCommandBuilderImpl<>(codec);
    }

    @Override
    public RedisFuture<Long> append(K key, V value) {
        RedisCommand<K, V, Long> cmd = commandBuilder.append(key, value);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> decr(K key) {
        RedisCommand<K, V, Long> cmd = commandBuilder.decr(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> decrby(K key, long amount) {
        RedisCommand<K, V, Long> cmd = commandBuilder.decrby(key, amount);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> del(K... keys) {
        Command<K, V, Long> cmd = commandBuilder.del(keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<Long> del(Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.del(keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<Long> exists(K... keys) {
        Command<K, V, Long> cmd = commandBuilder.exists(keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<Long> exists(Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.exists(keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<Boolean> expire(K key, long seconds) {
        Command<K, V, Boolean> cmd = commandBuilder.expire(key, seconds);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Boolean> expire(K key, Duration seconds) {
        LettuceAssert.notNull(seconds, "Timeout must not be null");
        return expire(key, seconds.toMillis() / 1000);
    }

    @Override
    public RedisFuture<Boolean> expireat(K key, long timestamp) {
        Command<K, V, Boolean> cmd = commandBuilder.expireat(key, timestamp);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Boolean> expireat(K key, Date timestamp) {
        LettuceAssert.notNull(timestamp, "Timestamp must not be null");
        return expireat(key, timestamp.getTime() / 1000);
    }

    @Override
    public RedisFuture<Boolean> expireat(K key, Instant timestamp) {
        LettuceAssert.notNull(timestamp, "Timestamp must not be null");
        return expireat(key, timestamp.toEpochMilli() / 1000);
    }

    @Override
    public RedisFuture<V> get(K key) {
        RedisCommand<K, V, V> cmd = commandBuilder.get(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> getbit(K key, long offset) {
        Command<K, V, Long> cmd = commandBuilder.getbit(key, offset);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<V> getdel(K key) {
        Command<K, V, V> cmd = commandBuilder.getdel(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<V> getex(K key, GetExArgs args) {
        Command<K, V, V> cmd = commandBuilder.getex(key, args);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<V> getrange(K key, long start, long end) {
        Command<K, V, V> cmd = commandBuilder.getrange(key, start, end);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<V> getset(K key, V value) {
        Command<K, V, V> cmd = commandBuilder.getset(key, value);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> hdel(K key, K... fields) {
        Command<K, V, Long> cmd = commandBuilder.hdel(key, fields);
        return dispatch(cmd, String.valueOf(key), RedisKeyUtil.generate(fields));
    }

    @Override
    public RedisFuture<Boolean> hexists(K key, K field) {
        Command<K, V, Boolean> cmd = commandBuilder.hexists(key, field);
        return dispatch(cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public RedisFuture<V> hget(K key, K field) {
        Command<K, V, V> cmd = commandBuilder.hget(key, field);
        return dispatch(cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public RedisFuture<Map<K, V>> hgetall(K key) {
        Command<K, V, Map<K, V>> cmd = commandBuilder.hgetall(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> hgetall(KeyValueStreamingChannel<K, V> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hgetall(channel, key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> hincrby(K key, K field, long amount) {
        Command<K, V, Long> cmd = commandBuilder.hincrby(key, field, amount);
        return dispatch(cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public RedisFuture<Double> hincrbyfloat(K key, K field, double amount) {
        Command<K, V, Double> cmd = commandBuilder.hincrbyfloat(key, field, amount);
        return dispatch(cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public RedisFuture<List<K>> hkeys(K key) {
        Command<K, V, List<K>> cmd = commandBuilder.hkeys(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> hkeys(KeyStreamingChannel<K> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hkeys(channel, key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> hlen(K key) {
        Command<K, V, Long> cmd = commandBuilder.hlen(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<List<KeyValue<K, V>>> hmget(K key, K... fields) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.hmgetKeyValue(key, fields);
        return dispatch(cmd, String.valueOf(key), RedisKeyUtil.generate(fields));
    }

    @Override
    public RedisFuture<Long> hmget(KeyValueStreamingChannel<K, V> channel, K key, K... fields) {
        Command<K, V, Long> cmd = commandBuilder.hmget(channel, key, fields);
        return dispatch(cmd, String.valueOf(key), RedisKeyUtil.generate(fields));
    }

    @Override
    public RedisFuture<String> hmset(K key, Map<K, V> map) {
        Command<K, V, String> cmd = commandBuilder.hmset(key, map);
        return dispatch(cmd, String.valueOf(key), RedisKeyUtil.generate(map));
    }

    @Override
    public RedisFuture<Boolean> hset(K key, K field, V value) {
        Command<K, V, Boolean> cmd = commandBuilder.hset(key, field, value);
        return dispatch(cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public RedisFuture<Long> hset(K key, Map<K, V> map) {
        Command<K, V, Long> cmd = commandBuilder.hset(key, map);
        return dispatch(cmd, String.valueOf(key), RedisKeyUtil.generate(map));
    }

    @Override
    public RedisFuture<Boolean> hsetnx(K key, K field, V value) {
        Command<K, V, Boolean> cmd = commandBuilder.hsetnx(key, field, value);
        return dispatch(cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public RedisFuture<List<V>> hvals(K key) {
        Command<K, V, List<V>> cmd = commandBuilder.hvals(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> hvals(ValueStreamingChannel<V> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hvals(channel, key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> incr(K key) {
        Command<K, V, Long> cmd = commandBuilder.incr(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> incrby(K key, long amount) {
        Command<K, V, Long> cmd = commandBuilder.incrby(key, amount);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Double> incrbyfloat(K key, double amount) {
        Command<K, V, Double> cmd = commandBuilder.incrbyfloat(key, amount);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<List<K>> keys(K pattern) {
        Command<K, V, List<K>> cmd = commandBuilder.keys(pattern);
        return dispatch(cmd, String.valueOf(pattern));
    }

    @Override
    public RedisFuture<Long> keys(KeyStreamingChannel<K> channel, K pattern) {
        Command<K, V, Long> cmd = commandBuilder.keys(channel, pattern);
        return dispatch(cmd, String.valueOf(pattern));
    }

    @Override
    public RedisFuture<V> lindex(K key, long index) {
        Command<K, V, V> cmd = commandBuilder.lindex(key, index);
        return dispatch(cmd, String.valueOf(key), RedisKeyUtil.generate("index", String.valueOf(index)));
    }

    @Override
    public RedisFuture<Long> llen(K key) {
        Command<K, V, Long> cmd = commandBuilder.llen(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<V> lpop(K key) {
        Command<K, V, V> cmd = commandBuilder.lpop(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<List<V>> lpop(K key, long count) {
        Command<K, V, List<V>> cmd = commandBuilder.lpop(key, count);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<List<V>> lrange(K key, long start, long stop) {
        Command<K, V, List<V>> cmd = commandBuilder.lrange(key, start, stop);
        return dispatch(cmd, String.valueOf(key),
            RedisKeyUtil.generate("start", String.valueOf(start), "stop", String.valueOf(stop)));
    }

    @Override
    public RedisFuture<Long> lrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        Command<K, V, Long> cmd = commandBuilder.lrange(channel, key, start, stop);
        return dispatch(cmd, String.valueOf(key),
            RedisKeyUtil.generate("start", String.valueOf(start), "stop", String.valueOf(stop)));
    }

    @Override
    public RedisFuture<String> lset(K key, long index, V value) {
        Command<K, V, String> cmd = commandBuilder.lset(key, index, value);
        return dispatch(cmd, String.valueOf(key), RedisKeyUtil.generate("index", String.valueOf(index)));
    }

    @Override
    public RedisFuture<String> ltrim(K key, long start, long stop) {
        Command<K, V, String> cmd = commandBuilder.ltrim(key, start, stop);
        return dispatch(cmd, String.valueOf(key),
            RedisKeyUtil.generate("start", String.valueOf(start), "stop", String.valueOf(stop)));
    }

    @Override
    public RedisFuture<List<KeyValue<K, V>>> mget(K... keys) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.mgetKeyValue(keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<List<KeyValue<K, V>>> mget(Iterable<K> keys) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.mgetKeyValue(keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<Long> mget(KeyValueStreamingChannel<K, V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.mget(channel, keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<Long> mget(KeyValueStreamingChannel<K, V> channel, Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.mget(channel, keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<String> mset(Map<K, V> map) {
        Command<K, V, String> cmd = commandBuilder.mset(map);
        return dispatch(cmd, RedisKeyUtil.generate(map));
    }

    @Override
    public RedisFuture<Boolean> msetnx(Map<K, V> map) {
        Command<K, V, Boolean> cmd = commandBuilder.msetnx(map);
        return dispatch(cmd, RedisKeyUtil.generate(map));
    }

    @Override
    public RedisFuture<Boolean> persist(K key) {
        Command<K, V, Boolean> cmd = commandBuilder.persist(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Boolean> pexpire(K key, long milliseconds) {
        Command<K, V, Boolean> cmd = commandBuilder.pexpire(key, milliseconds);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Boolean> pexpire(K key, Duration milliseconds) {
        LettuceAssert.notNull(milliseconds, "Timeout must not be null");
        return pexpire(key, milliseconds.toMillis());
    }

    @Override
    public RedisFuture<Boolean> pexpireat(K key, Date timestamp) {
        LettuceAssert.notNull(timestamp, "Timestamp must not be null");
        return pexpireat(key, timestamp.getTime());
    }

    @Override
    public RedisFuture<Boolean> pexpireat(K key, Instant timestamp) {
        LettuceAssert.notNull(timestamp, "Timestamp must not be null");
        return pexpireat(key, timestamp.toEpochMilli());
    }

    @Override
    public RedisFuture<Boolean> pexpireat(K key, long timestamp) {
        Command<K, V, Boolean> cmd = commandBuilder.pexpireat(key, timestamp);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<String> psetex(K key, long milliseconds, V value) {
        Command<K, V, String> cmd = commandBuilder.psetex(key, milliseconds, value);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> pttl(K key) {
        Command<K, V, Long> cmd = commandBuilder.pttl(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<V> rpop(K key) {
        Command<K, V, V> cmd = commandBuilder.rpop(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<List<V>> rpop(K key, long count) {
        Command<K, V, List<V>> cmd = commandBuilder.rpop(key, count);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<V> rpoplpush(K source, K destination) {
        Command<K, V, V> cmd = commandBuilder.rpoplpush(source, destination);
        return dispatch(cmd, String.valueOf(source), String.valueOf(destination));
    }

    @Override
    public RedisFuture<Long> scard(K key) {
        Command<K, V, Long> cmd = commandBuilder.scard(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Set<V>> sdiff(K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sdiff(keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<Long> sdiff(ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sdiff(channel, keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<String> set(K key, V value) {
        Command<K, V, String> cmd = commandBuilder.set(key, value);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<String> set(K key, V value, SetArgs setArgs) {
        Command<K, V, String> cmd = commandBuilder.set(key, value, setArgs);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<V> setGet(K key, V value) {
        Command<K, V, V> cmd = commandBuilder.setGet(key, value);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<V> setGet(K key, V value, SetArgs setArgs) {
        Command<K, V, V> cmd = commandBuilder.setGet(key, value, setArgs);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<String> setex(K key, long seconds, V value) {
        Command<K, V, String> cmd = commandBuilder.setex(key, seconds, value);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Boolean> setnx(K key, V value) {
        Command<K, V, Boolean> cmd = commandBuilder.setnx(key, value);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> setrange(K key, long offset, V value) {
        Command<K, V, Long> cmd = commandBuilder.setrange(key, offset, value);
        return dispatch(cmd, String.valueOf(key), RedisKeyUtil.generate("offset", String.valueOf(offset)));
    }

    @Override
    public RedisFuture<Set<V>> sinter(K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sinter(keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<Long> sinter(ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sinter(channel, keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<V> spop(K key) {
        Command<K, V, V> cmd = commandBuilder.spop(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Set<V>> spop(K key, long count) {
        Command<K, V, Set<V>> cmd = commandBuilder.spop(key, count);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<V> srandmember(K key) {
        Command<K, V, V> cmd = commandBuilder.srandmember(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<List<V>> srandmember(K key, long count) {
        Command<K, V, List<V>> cmd = commandBuilder.srandmember(key, count);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> srandmember(ValueStreamingChannel<V> channel, K key, long count) {
        Command<K, V, Long> cmd = commandBuilder.srandmember(channel, key, count);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> strlen(K key) {
        Command<K, V, Long> cmd = commandBuilder.strlen(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Set<V>> sunion(K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sunion(keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<Long> sunion(ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sunion(channel, keys);
        return dispatch(cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public RedisFuture<Long> ttl(K key) {
        Command<K, V, Long> cmd = commandBuilder.ttl(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<String> type(K key) {
        Command<K, V, String> cmd = commandBuilder.type(key);
        return dispatch(cmd, String.valueOf(key));
    }

    @Override
    public RedisFuture<Long> zcard(K key) {
        Command<K, V, Long> cmd = commandBuilder.zcard(key);
        return dispatch(cmd, String.valueOf(key));
    }

    private <T> AsyncCommand<K, V, T> dispatch(RedisCommand<K, V, T> cmd, String key) {
        return dispatch(cmd, key, null);
    }

    private <T> AsyncCommand<K, V, T> dispatch(RedisCommand<K, V, T> cmd, String key, String field) {
        if (redisUri == null) {
            redisUri = LettuceHelper.getRedisUri(this.getStatefulConnection().hashCode());
        }
        if (ContextManager.needReplay()) {
            AsyncCommand<K, V, T> asyncCommand = new AsyncCommand<>(cmd);
            RedisExtractor extractor = new RedisExtractor(this.redisUri, cmd.getType().name(), key, field);
            MockResult mockResult = extractor.replay();
            if (mockResult.notIgnoreMockResult()) {
                asyncCommand.complete((T) mockResult.getResult());
                return asyncCommand;
            }
        }

        AsyncCommand<K, V, T> resultFuture = super.dispatch(cmd);

        if (ContextManager.needRecord()) {
            try (TraceTransmitter traceTransmitter = TraceTransmitter.create()) {
                resultFuture.whenComplete((v, throwable) -> {
                    traceTransmitter.transmit();
                    RedisExtractor extractor = new RedisExtractor(this.redisUri, cmd.getType().name(), key, field);
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
