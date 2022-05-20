package io.arex.inst.lettuce.v6;

import io.arex.foundation.context.ContextManager;
import io.arex.inst.jedis.common.RedisExtractor;
import io.arex.inst.jedis.common.RedisKeyUtil;
import io.lettuce.core.GetExArgs;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisReactiveCommandsImpl;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.internal.LettuceAssert;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingChannel;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.RedisCommand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * RedisReactiveCommandsImplWrapper
 */
public class RedisReactiveCommandsImplWrapper<K, V> extends RedisReactiveCommandsImpl<K, V> {
    private final RedisCommandBuilderImpl<K, V> commandBuilder;
    private String redisUri;

    /**
     * Initialize a new instance.
     *
     * @param connection the connection to operate on.
     * @param codec      the codec for command encoding.
     */
    public RedisReactiveCommandsImplWrapper(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
        this.commandBuilder = new RedisCommandBuilderImpl<>(codec);
    }

    @Override
    public Mono<Long> append(K key, V value) {
        RedisCommand<K, V, Long> cmd = commandBuilder.append(key, value);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> decr(K key) {
        RedisCommand<K, V, Long> cmd = commandBuilder.decr(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> decrby(K key, long amount) {
        RedisCommand<K, V, Long> cmd = commandBuilder.decrby(key, amount);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> del(K... keys) {
        Command<K, V, Long> cmd = commandBuilder.del(keys);
        return createMono(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<Long> del(Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.del(keys);
        return createMono(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<Long> exists(K... keys) {
        Command<K, V, Long> cmd = commandBuilder.exists(keys);
        return createMono(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<Long> exists(Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.exists(keys);
        return createMono(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<Boolean> expire(K key, long seconds) {
        Command<K, V, Boolean> cmd = commandBuilder.expire(key, seconds);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Boolean> expire(K key, Duration seconds) {
        LettuceAssert.notNull(seconds, "Timeout must not be null");
        return expire(key, seconds.toMillis() / 1000);
    }

    @Override
    public Mono<Boolean> expireat(K key, long timestamp) {
        Command<K, V, Boolean> cmd = commandBuilder.expireat(key, timestamp);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Boolean> expireat(K key, Date timestamp) {
        LettuceAssert.notNull(timestamp, "Timestamp must not be null");
        return expireat(key, timestamp.getTime() / 1000);
    }

    @Override
    public Mono<Boolean> expireat(K key, Instant timestamp) {
        LettuceAssert.notNull(timestamp, "Timestamp must not be null");
        return expireat(key, timestamp.toEpochMilli() / 1000);
    }

    @Override
    public Mono<V> get(K key) {
        RedisCommand<K, V, V> cmd = commandBuilder.get(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> getbit(K key, long offset) {
        Command<K, V, Long> cmd = commandBuilder.getbit(key, offset);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<V> getdel(K key) {
        Command<K, V, V> cmd = commandBuilder.getdel(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<V> getex(K key, GetExArgs args) {
        Command<K, V, V> cmd = commandBuilder.getex(key, args);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<V> getrange(K key, long start, long end) {
        Command<K, V, V> cmd = commandBuilder.getrange(key, start, end);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<V> getset(K key, V value) {
        Command<K, V, V> cmd = commandBuilder.getset(key, value);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> hdel(K key, K... fields) {
        Command<K, V, Long> cmd = commandBuilder.hdel(key, fields);
        return createMono(() -> cmd, String.valueOf(key), RedisKeyUtil.generate(fields));
    }

    @Override
    public Mono<Boolean> hexists(K key, K field) {
        Command<K, V, Boolean> cmd = commandBuilder.hexists(key, field);
        return createMono(() -> cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public Mono<V> hget(K key, K field) {
        Command<K, V, V> cmd = commandBuilder.hget(key, field);
        return createMono(() -> cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public Flux<KeyValue<K, V>> hgetall(K key) {
        Command<K, V, Map<K, V>> cmd = commandBuilder.hgetall(key);
        return createDissolvingFlux(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> hgetall(KeyValueStreamingChannel<K, V> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hgetall(channel, key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> hincrby(K key, K field, long amount) {
        Command<K, V, Long> cmd = commandBuilder.hincrby(key, field, amount);
        return createMono(() -> cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public Mono<Double> hincrbyfloat(K key, K field, double amount) {
        Command<K, V, Double> cmd = commandBuilder.hincrbyfloat(key, field, amount);
        return createMono(() -> cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public Flux<K> hkeys(K key) {
        Command<K, V, List<K>> cmd = commandBuilder.hkeys(key);
        return createDissolvingFlux(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> hkeys(KeyStreamingChannel<K> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hkeys(channel, key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> hlen(K key) {
        Command<K, V, Long> cmd = commandBuilder.hlen(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Flux<KeyValue<K, V>> hmget(K key, K... fields) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.hmgetKeyValue(key, fields);
        return createDissolvingFlux(() -> cmd, String.valueOf(key), RedisKeyUtil.generate(fields));
    }

    @Override
    public Mono<Long> hmget(KeyValueStreamingChannel<K, V> channel, K key, K... fields) {
        Command<K, V, Long> cmd = commandBuilder.hmget(channel, key, fields);
        return createMono(() -> cmd, String.valueOf(key), RedisKeyUtil.generate(fields));
    }

    @Override
    public Mono<String> hmset(K key, Map<K, V> map) {
        Command<K, V, String> cmd = commandBuilder.hmset(key, map);
        return createMono(() -> cmd, String.valueOf(key), RedisKeyUtil.generate(map));
    }

    @Override
    public Mono<Boolean> hset(K key, K field, V value) {
        Command<K, V, Boolean> cmd = commandBuilder.hset(key, field, value);
        return createMono(() -> cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public Mono<Long> hset(K key, Map<K, V> map) {
        Command<K, V, Long> cmd = commandBuilder.hset(key, map);
        return createMono(() -> cmd, String.valueOf(key), RedisKeyUtil.generate(map));
    }

    @Override
    public Mono<Boolean> hsetnx(K key, K field, V value) {
        Command<K, V, Boolean> cmd = commandBuilder.hsetnx(key, field, value);
        return createMono(() -> cmd, String.valueOf(key), String.valueOf(field));
    }

    @Override
    public Flux<V> hvals(K key) {
        Command<K, V, List<V>> cmd = commandBuilder.hvals(key);
        return createDissolvingFlux(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> hvals(ValueStreamingChannel<V> channel, K key) {
        Command<K, V, Long> cmd = commandBuilder.hvals(channel, key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> incr(K key) {
        Command<K, V, Long> cmd = commandBuilder.incr(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> incrby(K key, long amount) {
        Command<K, V, Long> cmd = commandBuilder.incrby(key, amount);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Double> incrbyfloat(K key, double amount) {
        Command<K, V, Double> cmd = commandBuilder.incrbyfloat(key, amount);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Flux<K> keys(K pattern) {
        Command<K, V, List<K>> cmd = commandBuilder.keys(pattern);
        return createDissolvingFlux(() -> cmd, String.valueOf(pattern));
    }

    @Override
    public Mono<Long> keys(KeyStreamingChannel<K> channel, K pattern) {
        Command<K, V, Long> cmd = commandBuilder.keys(channel, pattern);
        return createMono(() -> cmd, String.valueOf(pattern));
    }

    @Override
    public Mono<V> lindex(K key, long index) {
        Command<K, V, V> cmd = commandBuilder.lindex(key, index);
        return createMono(() -> cmd, String.valueOf(key), RedisKeyUtil.generate("index", String.valueOf(index)));
    }

    @Override
    public Mono<Long> llen(K key) {
        Command<K, V, Long> cmd = commandBuilder.llen(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<V> lpop(K key) {
        Command<K, V, V> cmd = commandBuilder.lpop(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Flux<V> lpop(K key, long count) {
        Command<K, V, List<V>> cmd = commandBuilder.lpop(key, count);
        return createDissolvingFlux(() -> cmd, String.valueOf(key));
    }

    @Override
    public Flux<V> lrange(K key, long start, long stop) {
        Command<K, V, List<V>> cmd = commandBuilder.lrange(key, start, stop);
        return createDissolvingFlux(() -> cmd, String.valueOf(key),
            RedisKeyUtil.generate("start", String.valueOf(start), "stop", String.valueOf(stop)));
    }

    @Override
    public Mono<Long> lrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        Command<K, V, Long> cmd = commandBuilder.lrange(channel, key, start, stop);
        return createMono(() -> cmd, String.valueOf(key),
            RedisKeyUtil.generate("start", String.valueOf(start), "stop", String.valueOf(stop)));
    }

    @Override
    public Mono<String> lset(K key, long index, V value) {
        Command<K, V, String> cmd = commandBuilder.lset(key, index, value);
        return createMono(() -> cmd, String.valueOf(key), RedisKeyUtil.generate("index", String.valueOf(index)));
    }

    @Override
    public Mono<String> ltrim(K key, long start, long stop) {
        Command<K, V, String> cmd = commandBuilder.ltrim(key, start, stop);
        return createMono(() -> cmd, String.valueOf(key),
            RedisKeyUtil.generate("start", String.valueOf(start), "stop", String.valueOf(stop)));
    }

    @Override
    public Flux<KeyValue<K, V>> mget(K... keys) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.mgetKeyValue(keys);
        return createDissolvingFlux(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Flux<KeyValue<K, V>> mget(Iterable<K> keys) {
        Command<K, V, List<KeyValue<K, V>>> cmd = commandBuilder.mgetKeyValue(keys);
        return createDissolvingFlux(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<Long> mget(KeyValueStreamingChannel<K, V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.mget(channel, keys);
        return createMono(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<Long> mget(KeyValueStreamingChannel<K, V> channel, Iterable<K> keys) {
        Command<K, V, Long> cmd = commandBuilder.mget(channel, keys);
        return createMono(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<String> mset(Map<K, V> map) {
        Command<K, V, String> cmd = commandBuilder.mset(map);
        return createMono(() -> cmd, RedisKeyUtil.generate(map));
    }

    @Override
    public Mono<Boolean> msetnx(Map<K, V> map) {
        Command<K, V, Boolean> cmd = commandBuilder.msetnx(map);
        return createMono(() -> cmd, RedisKeyUtil.generate(map));
    }

    @Override
    public Mono<Boolean> persist(K key) {
        Command<K, V, Boolean> cmd = commandBuilder.persist(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Boolean> pexpire(K key, long milliseconds) {
        Command<K, V, Boolean> cmd = commandBuilder.pexpire(key, milliseconds);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Boolean> pexpire(K key, Duration milliseconds) {
        LettuceAssert.notNull(milliseconds, "Timeout must not be null");
        return pexpire(key, milliseconds.toMillis());
    }

    @Override
    public Mono<Boolean> pexpireat(K key, Date timestamp) {
        LettuceAssert.notNull(timestamp, "Timestamp must not be null");
        return pexpireat(key, timestamp.getTime());
    }

    @Override
    public Mono<Boolean> pexpireat(K key, Instant timestamp) {
        LettuceAssert.notNull(timestamp, "Timestamp must not be null");
        return pexpireat(key, timestamp.toEpochMilli());
    }

    @Override
    public Mono<Boolean> pexpireat(K key, long timestamp) {
        Command<K, V, Boolean> cmd = commandBuilder.pexpireat(key, timestamp);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<String> psetex(K key, long milliseconds, V value) {
        Command<K, V, String> cmd = commandBuilder.psetex(key, milliseconds, value);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> pttl(K key) {
        Command<K, V, Long> cmd = commandBuilder.pttl(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<V> rpop(K key) {
        Command<K, V, V> cmd = commandBuilder.rpop(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Flux<V> rpop(K key, long count) {
        Command<K, V, List<V>> cmd = commandBuilder.rpop(key, count);
        return createDissolvingFlux(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<V> rpoplpush(K source, K destination) {
        Command<K, V, V> cmd = commandBuilder.rpoplpush(source, destination);
        return createMono(() -> cmd, String.valueOf(source), String.valueOf(destination));
    }

    @Override
    public Mono<Long> scard(K key) {
        Command<K, V, Long> cmd = commandBuilder.scard(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Flux<V> sdiff(K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sdiff(keys);
        return createDissolvingFlux(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<Long> sdiff(ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sdiff(channel, keys);
        return createMono(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<String> set(K key, V value) {
        Command<K, V, String> cmd = commandBuilder.set(key, value);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<String> set(K key, V value, SetArgs setArgs) {
        Command<K, V, String> cmd = commandBuilder.set(key, value, setArgs);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<V> setGet(K key, V value) {
        Command<K, V, V> cmd = commandBuilder.setGet(key, value);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<V> setGet(K key, V value, SetArgs setArgs) {
        Command<K, V, V> cmd = commandBuilder.setGet(key, value, setArgs);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<String> setex(K key, long seconds, V value) {
        Command<K, V, String> cmd = commandBuilder.setex(key, seconds, value);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Boolean> setnx(K key, V value) {
        Command<K, V, Boolean> cmd = commandBuilder.setnx(key, value);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> setrange(K key, long offset, V value) {
        Command<K, V, Long> cmd = commandBuilder.setrange(key, offset, value);
        return createMono(() -> cmd, String.valueOf(key), RedisKeyUtil.generate("offset", String.valueOf(offset)));
    }

    @Override
    public Flux<V> sinter(K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sinter(keys);
        return createDissolvingFlux(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<Long> sinter(ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sinter(channel, keys);
        return createMono(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<V> spop(K key) {
        Command<K, V, V> cmd = commandBuilder.spop(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Flux<V> spop(K key, long count) {
        Command<K, V, Set<V>> cmd = commandBuilder.spop(key, count);
        return createDissolvingFlux(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<V> srandmember(K key) {
        Command<K, V, V> cmd = commandBuilder.srandmember(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Flux<V> srandmember(K key, long count) {
        Command<K, V, List<V>> cmd = commandBuilder.srandmember(key, count);
        return createDissolvingFlux(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> srandmember(ValueStreamingChannel<V> channel, K key, long count) {
        Command<K, V, Long> cmd = commandBuilder.srandmember(channel, key, count);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> strlen(K key) {
        Command<K, V, Long> cmd = commandBuilder.strlen(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Flux<V> sunion(K... keys) {
        Command<K, V, Set<V>> cmd = commandBuilder.sunion(keys);
        return createDissolvingFlux(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<Long> sunion(ValueStreamingChannel<V> channel, K... keys) {
        Command<K, V, Long> cmd = commandBuilder.sunion(channel, keys);
        return createMono(() -> cmd, RedisKeyUtil.generate(keys));
    }

    @Override
    public Mono<Long> ttl(K key) {
        Command<K, V, Long> cmd = commandBuilder.ttl(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<String> type(K key) {
        Command<K, V, String> cmd = commandBuilder.type(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    @Override
    public Mono<Long> zcard(K key) {
        Command<K, V, Long> cmd = commandBuilder.zcard(key);
        return createMono(() -> cmd, String.valueOf(key));
    }

    public <T> Mono<T> createMono(Supplier<RedisCommand<K, V, T>> commandSupplier, String key) {
        return createMono(commandSupplier, key, null);
    }

    public <T> Mono<T> createMono(Supplier<RedisCommand<K, V, T>> commandSupplier, String key, String field) {
        if (redisUri == null) {
            redisUri = LettuceHelper.getRedisUri(this.getStatefulConnection().hashCode());
        }

        if (ContextManager.needReplay()) {
            return Mono.fromCallable(() -> {
                RedisExtractor extractor =
                    new RedisExtractor(this.redisUri, commandSupplier.get().getType().name(), key, field);
                return (T) extractor.replay();
            });
        }

        return super.createMono(commandSupplier).doOnNext(result -> {
            if (ContextManager.needRecord()) {
                RedisExtractor extractor =
                    new RedisExtractor(this.redisUri, commandSupplier.get().getType().name(), key, field);
                extractor.record(result);
            }
        }).doOnError(throwable -> {
            if (ContextManager.needRecord()) {
                RedisExtractor extractor =
                    new RedisExtractor(this.redisUri, commandSupplier.get().getType().name(), key, field);
                extractor.record(throwable);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T, R> Flux<R> createDissolvingFlux(Supplier<RedisCommand<K, V, T>> commandSupplier, String key) {
        return createDissolvingFlux(commandSupplier, key, null);
    }

    @SuppressWarnings("unchecked")
    public <T, R> Flux<R> createDissolvingFlux(Supplier<RedisCommand<K, V, T>> commandSupplier, String key,
        String field) {
        if (redisUri == null) {
            redisUri = LettuceHelper.getRedisUri(this.getStatefulConnection().hashCode());
        }

        if (ContextManager.needReplay()) {
            return Flux.fromStream(() -> {
                RedisExtractor extractor =
                    new RedisExtractor(this.redisUri, commandSupplier.get().getType().name(), key, field);
                return Stream.of((R) extractor.replay());
            });
        }

        return (Flux<R>) super.createDissolvingFlux(commandSupplier).doOnNext(result -> {
            if (ContextManager.needRecord()) {
                RedisExtractor extractor =
                    new RedisExtractor(this.redisUri, commandSupplier.get().getType().name(), key, field);
                extractor.record(result);
            }
        }).doOnError(throwable -> {
            if (ContextManager.needRecord()) {
                RedisExtractor extractor =
                    new RedisExtractor(this.redisUri, commandSupplier.get().getType().name(), key, field);
                extractor.record(throwable);
            }
        });
    }
}
