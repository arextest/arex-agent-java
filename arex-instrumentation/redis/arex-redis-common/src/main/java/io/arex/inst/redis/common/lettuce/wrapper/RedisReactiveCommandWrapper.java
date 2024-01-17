package io.arex.inst.redis.common.lettuce.wrapper;

import io.arex.inst.redis.common.RedisKeyUtil;
import io.arex.inst.redis.common.lettuce.ReactorStreamUtil;
import io.arex.inst.redis.common.lettuce.RedisCommandBuilderImpl;
import io.arex.inst.runtime.context.ContextManager;
import io.lettuce.core.AbstractRedisReactiveCommands;
import io.lettuce.core.GetExArgs;
import io.lettuce.core.KeyValue;
import io.lettuce.core.SetArgs;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.internal.LettuceAssert;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingChannel;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.RedisCommand;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * RedisReactiveCommandsImplWrapper
 */
public class RedisReactiveCommandWrapper<K, V> {

    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String INDEX = "index";
    private static final String TIMESTAMP_LOG = "Timestamp must not be null";
    private static final String TIMEOUT_LOG = "Timeout must not be null";

    private final RedisCommandBuilderImpl<K, V> commandBuilder;

    public RedisReactiveCommandWrapper(RedisCodec<K, V> codec) {
        this.commandBuilder = new RedisCommandBuilderImpl<>(codec);
    }


    public Mono<Long> append(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        V value) {
        RedisCommand<K, V, Long> cmd = commandBuilder.append(key, value);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> decr(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        RedisCommand<K, V, Long> cmd = commandBuilder.decr(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> decrby(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long amount) {
        RedisCommand<K, V, Long> cmd = commandBuilder.decrby(key, amount);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> del(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.del(keys);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<Long> del(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.del(keys);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<Long> exists(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K... keys) {
        Command<K, V, Long> cmd = commandBuilder.exists(keys);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<Long> exists(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.exists(keys);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<Boolean> expire(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long seconds) {
        Command<K, V, Boolean> cmd = commandBuilder.expire(key, seconds);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Boolean> expire(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        Duration seconds) {
        LettuceAssert.notNull(seconds, TIMEOUT_LOG);
        return expire(redisReactiveCommands, redisUri, key, seconds.toMillis() / 1000);
    }


    public Mono<Boolean> expireat(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, long timestamp) {
        Command<K, V, Boolean> cmd = commandBuilder.expireat(key, timestamp);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Boolean> expireat(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, Date timestamp) {
        LettuceAssert.notNull(timestamp, TIMESTAMP_LOG);
        return expireat(redisReactiveCommands, redisUri, key, timestamp.getTime() / 1000);
    }


    public Mono<Boolean> expireat(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, Instant timestamp) {
        LettuceAssert.notNull(timestamp, TIMESTAMP_LOG);
        return expireat(redisReactiveCommands, redisUri, key, timestamp.toEpochMilli() / 1000);
    }


    public Mono<V> get(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        RedisCommand<K, V, V> cmd = commandBuilder.get(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> getbit(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long offset) {
        Command<K, V, Long> cmd = commandBuilder.getbit(key, offset);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<V> getdel(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, V> cmd = commandBuilder.getdel(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<V> getex(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        GetExArgs args) {
        Command<K, V, V> cmd = commandBuilder.getex(key, args);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<V> getrange(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long start, long end) {
        Command<K, V, V> cmd = commandBuilder.getrange(key, start, end);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<V> getset(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        V value) {
        Command<K, V, V> cmd = commandBuilder.getset(key, value);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }

    public Mono<Long> hdel(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        K... fields) {
        Command<K, V, Long> cmd = commandBuilder.hdel(key, fields);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key),
            RedisKeyUtil.generate(fields));
    }

    public Mono<Boolean> hexists(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, K field) {
        Command<K, V, Boolean> cmd = commandBuilder.hexists(key, field);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key), String.valueOf(field));
    }

    public Mono<V> hget(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        K field) {
        Command<K, V, V> cmd = commandBuilder.hget(key, field);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key), String.valueOf(field));
    }

    public Mono<Long> hgetall(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        KeyValueStreamingChannel<K, V> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hgetall(channel, key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }

    /**
     * hgetallFlux支持v6版本的hgetAll
     * @param redisReactiveCommands
     * @param redisUri
     * @param key
     * @return
     */

    public Flux<KeyValue<K, V>> hgetallFlux(AbstractRedisReactiveCommands<K, V> redisReactiveCommands,
        final String redisUri, K key) {
        Command<K, V, Map<K, V>> cmd = commandBuilder.hgetall(key);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }

    /**
     * hgetallMono支持v5版本的hgetAll
     * @param redisReactiveCommands
     * @param redisUri
     * @param key
     * @return
     */
    public  Mono<Map<K, V>>  hgetallMono(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,K key) {
        Command<K, V, Map<K, V>> cmd = commandBuilder.hgetall(key);
        return createMono(redisReactiveCommands, redisUri,() -> cmd, String.valueOf(key));
    }

    public Mono<Long> hincrby(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        K field, long amount) {
        Command<K, V, Long> cmd = commandBuilder.hincrby(key, field, amount);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key), String.valueOf(field));
    }


    public Mono<Double> hincrbyfloat(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, K field, double amount) {
        Command<K, V, Double> cmd = commandBuilder.hincrbyfloat(key, field, amount);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key), String.valueOf(field));
    }


    public Flux<K> hkeys(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, List<K>> cmd = commandBuilder.hkeys(key);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> hkeys(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,KeyStreamingChannel<K> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hkeys(channel, key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> hlen(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, Long> cmd = commandBuilder.hlen(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Flux<KeyValue<K, V>> hmget(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, K... fields) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.hmgetKeyValue(key, fields);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key),
            RedisKeyUtil.generate(fields));
    }


    public Mono<Long> hmget(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        KeyValueStreamingChannel<K, V> channel, K key, K... fields) {
        Command<K, V, Long> cmd = commandBuilder.hmget(channel, key, fields);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key),
            RedisKeyUtil.generate(fields));
    }


    public Mono<String> hmset(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        Map<K, V> map) {
        Command<K, V, String> cmd = commandBuilder.hmset(key, map);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key), RedisKeyUtil.generate(map));
    }


    public Mono<Boolean> hset(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        K field, V value) {
        Command<K, V, Boolean> cmd = commandBuilder.hset(key, field, value);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key), String.valueOf(field));
    }


    public Mono<Long> hset(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        Map<K, V> map) {
        Command<K, V, Long> cmd = commandBuilder.hset(key, map);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key), RedisKeyUtil.generate(map));
    }


    public Mono<Boolean> hsetnx(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        K field, V value) {
        Command<K, V, Boolean> cmd = commandBuilder.hsetnx(key, field, value);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key), String.valueOf(field));
    }


    public Flux<V> hvals(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, List<V>> cmd = commandBuilder.hvals(key);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> hvals(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        ValueStreamingChannel<V> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hvals(channel, key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> incr(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, Long> cmd = commandBuilder.incr(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> incrby(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long amount) {
        Command<K, V, Long> cmd = commandBuilder.incrby(key, amount);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Double> incrbyfloat(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, double amount) {
        Command<K, V, Double> cmd = commandBuilder.incrbyfloat(key, amount);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Flux<K> keys(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K pattern) {
        Command<K, V, List<K>> cmd = commandBuilder.keys(pattern);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(pattern));
    }


    public Mono<Long> keys(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        KeyStreamingChannel<K> channel, K pattern) {
        Command<K, V, Long> cmd = commandBuilder.keys(channel, pattern);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(pattern));
    }


    public Mono<V> lindex(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long index) {
        Command<K, V, V> cmd = commandBuilder.lindex(key, index);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key),
            RedisKeyUtil.generate(INDEX, String.valueOf(index)));
    }


    public Mono<Long> llen(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, Long> cmd = commandBuilder.llen(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<V> lpop(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, V> cmd = commandBuilder.lpop(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Flux<V> lpop(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long count) {
        Command<K, V, List<V>> cmd = commandBuilder.lpop(key, count);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Flux<V> lrange(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long start, long stop) {
        Command<K, V, List<V>> cmd = commandBuilder.lrange(key, start, stop);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key),
            RedisKeyUtil.generate(START, String.valueOf(start), STOP, String.valueOf(stop)));
    }


    public Mono<Long> lrange(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        ValueStreamingChannel<V> channel, K key, long start, long stop) {
        Command<K, V, Long> cmd = commandBuilder.lrange(channel, key, start, stop);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key),
            RedisKeyUtil.generate(START, String.valueOf(start), STOP, String.valueOf(stop)));
    }


    public Mono<String> lset(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long index, V value) {
        Command<K, V, String> cmd = commandBuilder.lset(key, index, value);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key),
            RedisKeyUtil.generate(INDEX, String.valueOf(index)));
    }


    public Mono<String> ltrim(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long start, long stop) {
        Command<K, V, String> cmd = commandBuilder.ltrim(key, start, stop);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key),
            RedisKeyUtil.generate(START, String.valueOf(start), STOP, String.valueOf(stop)));
    }


    public Flux<KeyValue<K, V>> mget(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K... keys) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.mgetKeyValue(keys);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Flux<KeyValue<K, V>> mget(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        Iterable<K> keys) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.mgetKeyValue(keys);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<Long> mget(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        KeyValueStreamingChannel<K, V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.mget(channel, keys);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<Long> mget(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        KeyValueStreamingChannel<K, V> channel, Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.mget(channel, keys);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<String> mset(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        Map<K, V> map) {
        Command<K, V, String> cmd = commandBuilder.mset(map);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(map));
    }


    public Mono<Boolean> msetnx(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        Map<K, V> map) {
        Command<K, V, Boolean> cmd = commandBuilder.msetnx(map);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(map));
    }


    public Mono<Boolean> persist(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key) {
        Command<K, V, Boolean> cmd = commandBuilder.persist(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Boolean> pexpire(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, long milliseconds) {
        Command<K, V, Boolean> cmd = commandBuilder.pexpire(key, milliseconds);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Boolean> pexpire(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, Duration milliseconds) {
        LettuceAssert.notNull(milliseconds, TIMEOUT_LOG);
        return pexpire(redisReactiveCommands, redisUri, key, milliseconds.toMillis());
    }


    public Mono<Boolean> pexpireat(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, Date timestamp) {
        LettuceAssert.notNull(timestamp, TIMESTAMP_LOG);
        return pexpireat(redisReactiveCommands, redisUri, key, timestamp.getTime());
    }


    public Mono<Boolean> pexpireat(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, Instant timestamp) {
        LettuceAssert.notNull(timestamp, TIMESTAMP_LOG);
        return pexpireat(redisReactiveCommands, redisUri, key, timestamp.toEpochMilli());
    }


    public Mono<Boolean> pexpireat(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, long timestamp) {
        Command<K, V, Boolean> cmd = commandBuilder.pexpireat(key, timestamp);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<String> psetex(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long milliseconds, V value) {
        Command<K, V, String> cmd = commandBuilder.psetex(key, milliseconds, value);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> pttl(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, Long> cmd = commandBuilder.pttl(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<String> rename(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        K newKey) {
        Command<K, V, String> cmd = commandBuilder.rename(key, newKey);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(key, newKey));
    }


    public Mono<Boolean> renamenx(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key, K newKey) {
        Command<K, V, Boolean> cmd = commandBuilder.renamenx(key, newKey);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(key, newKey));

    }


    public Mono<V> rpop(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, V> cmd = commandBuilder.rpop(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Flux<V> rpop(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long count) {
        Command<K, V, List<V>> cmd = commandBuilder.rpop(key, count);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<V> rpoplpush(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K source,
        K destination) {
        Command<K, V, V> cmd = commandBuilder.rpoplpush(source, destination);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(source),
            String.valueOf(destination));
    }


    public Mono<Long> scard(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, Long> cmd = commandBuilder.scard(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Flux<V> sdiff(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sdiff(keys);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<Long> sdiff(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sdiff(channel, keys);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<String> set(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        V value) {
        Command<K, V, String> cmd = commandBuilder.set(key, value);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<String> set(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        V value, SetArgs setArgs) {
        Command<K, V, String> cmd = commandBuilder.set(key, value, setArgs);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<V> setGet(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        V value) {
        Command<K, V, V> cmd = commandBuilder.setGet(key, value);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<V> setGet(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        V value, SetArgs setArgs) {
        Command<K, V, V> cmd = commandBuilder.setGet(key, value, setArgs);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<String> setex(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long seconds, V value) {
        Command<K, V, String> cmd = commandBuilder.setex(key, seconds, value);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Boolean> setnx(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        V value) {
        Command<K, V, Boolean> cmd = commandBuilder.setnx(key, value);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> setrange(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long offset, V value) {
        Command<K, V, Long> cmd = commandBuilder.setrange(key, offset, value);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key),
            RedisKeyUtil.generate("offset", String.valueOf(offset)));
    }


    public Flux<V> sinter(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sinter(keys);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<Long> sinter(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sinter(channel, keys);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<V> spop(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, V> cmd = commandBuilder.spop(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Flux<V> spop(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long count) {
        Command<K, V, Set<V>> cmd = commandBuilder.spop(key, count);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<V> srandmember(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        K key) {
        Command<K, V, V> cmd = commandBuilder.srandmember(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Flux<V> srandmember(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key,
        long count) {
        Command<K, V, List<V>> cmd = commandBuilder.srandmember(key, count);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> srandmember(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        ValueStreamingChannel<V> channel, K key, long count) {
        Command<K, V, Long> cmd = commandBuilder.srandmember(channel, key, count);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> strlen(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, Long> cmd = commandBuilder.strlen(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Flux<V> sunion(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sunion(keys);
        return createDissolvingFlux(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<Long> sunion(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sunion(channel, keys);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, RedisKeyUtil.generate(keys));
    }


    public Mono<Long> ttl(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, Long> cmd = commandBuilder.ttl(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<String> type(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, String> cmd = commandBuilder.type(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }


    public Mono<Long> zcard(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri, K key) {
        Command<K, V, Long> cmd = commandBuilder.zcard(key);
        return createMono(redisReactiveCommands, redisUri, () -> cmd, String.valueOf(key));
    }

    public <T> Mono<T> createMono(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        Supplier<RedisCommand<K, V, T>> commandSupplier,
        String key) {
        return createMono(redisReactiveCommands, redisUri, commandSupplier, key, null);
    }

    public <T> Mono<T> createMono(AbstractRedisReactiveCommands<K, V> redisReactiveCommands, final String redisUri,
        Supplier<RedisCommand<K, V, T>> commandSupplier,
        String key, String field) {
        if (ContextManager.needReplay()) {
            return (Mono<T>) ReactorStreamUtil.monoReplay(redisUri, commandSupplier.get().getType().name(), key, field);
        }
        Mono<T> monoResult = redisReactiveCommands.createMono(commandSupplier);
        if (ContextManager.needRecord()) {
            return (Mono<T>) ReactorStreamUtil.monoRecord(redisUri, monoResult, commandSupplier.get().getType().name(), key, field);
        }
        return monoResult;
    }

    public <T, R> Flux<R> createDissolvingFlux(AbstractRedisReactiveCommands<K, V> redisReactiveCommands,
        final String redisUri, Supplier<RedisCommand<K, V, T>> commandSupplier,
        String key) {
        return createDissolvingFlux(redisReactiveCommands, redisUri, commandSupplier, key, null);
    }

    @SuppressWarnings("unchecked")
    public <T, R> Flux<R> createDissolvingFlux(AbstractRedisReactiveCommands<K, V> redisReactiveCommands,
        final String redisUri, Supplier<RedisCommand<K, V, T>> commandSupplier,
        String key, String field) {

        if (ContextManager.needReplay()) {
            return (Flux<R>) ReactorStreamUtil.fluxReplay(redisUri, commandSupplier.get().getType().name(), key, field);
        }
        Flux<R> fluxResult  = redisReactiveCommands.createDissolvingFlux(commandSupplier);
        if (ContextManager.needRecord()) {
            ReactorStreamUtil.fluxRecord(redisUri, fluxResult, commandSupplier.get().getType().name(), key, field);
        }
        return fluxResult;
    }
}
