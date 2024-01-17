package io.arex.inst.redis.common.lettuce;

import static io.lettuce.core.protocol.CommandType.APPEND;
import static io.lettuce.core.protocol.CommandType.DECR;
import static io.lettuce.core.protocol.CommandType.DECRBY;
import static io.lettuce.core.protocol.CommandType.DEL;
import static io.lettuce.core.protocol.CommandType.EXISTS;
import static io.lettuce.core.protocol.CommandType.EXPIRE;
import static io.lettuce.core.protocol.CommandType.EXPIREAT;
import static io.lettuce.core.protocol.CommandType.GET;
import static io.lettuce.core.protocol.CommandType.GETBIT;
import static io.lettuce.core.protocol.CommandType.GETDEL;
import static io.lettuce.core.protocol.CommandType.GETEX;
import static io.lettuce.core.protocol.CommandType.GETRANGE;
import static io.lettuce.core.protocol.CommandType.GETSET;
import static io.lettuce.core.protocol.CommandType.HDEL;
import static io.lettuce.core.protocol.CommandType.HEXISTS;
import static io.lettuce.core.protocol.CommandType.HGET;
import static io.lettuce.core.protocol.CommandType.HGETALL;
import static io.lettuce.core.protocol.CommandType.HINCRBY;
import static io.lettuce.core.protocol.CommandType.HINCRBYFLOAT;
import static io.lettuce.core.protocol.CommandType.HKEYS;
import static io.lettuce.core.protocol.CommandType.HLEN;
import static io.lettuce.core.protocol.CommandType.HMGET;
import static io.lettuce.core.protocol.CommandType.HMSET;
import static io.lettuce.core.protocol.CommandType.HSET;
import static io.lettuce.core.protocol.CommandType.HSETNX;
import static io.lettuce.core.protocol.CommandType.HVALS;
import static io.lettuce.core.protocol.CommandType.INCR;
import static io.lettuce.core.protocol.CommandType.INCRBY;
import static io.lettuce.core.protocol.CommandType.INCRBYFLOAT;
import static io.lettuce.core.protocol.CommandType.KEYS;
import static io.lettuce.core.protocol.CommandType.LINDEX;
import static io.lettuce.core.protocol.CommandType.LLEN;
import static io.lettuce.core.protocol.CommandType.LPOP;
import static io.lettuce.core.protocol.CommandType.LRANGE;
import static io.lettuce.core.protocol.CommandType.LSET;
import static io.lettuce.core.protocol.CommandType.LTRIM;
import static io.lettuce.core.protocol.CommandType.MGET;
import static io.lettuce.core.protocol.CommandType.MSET;
import static io.lettuce.core.protocol.CommandType.MSETNX;
import static io.lettuce.core.protocol.CommandType.PERSIST;
import static io.lettuce.core.protocol.CommandType.PEXPIRE;
import static io.lettuce.core.protocol.CommandType.PEXPIREAT;
import static io.lettuce.core.protocol.CommandType.PSETEX;
import static io.lettuce.core.protocol.CommandType.PTTL;
import static io.lettuce.core.protocol.CommandType.RENAME;
import static io.lettuce.core.protocol.CommandType.RENAMENX;
import static io.lettuce.core.protocol.CommandType.RPOP;
import static io.lettuce.core.protocol.CommandType.RPOPLPUSH;
import static io.lettuce.core.protocol.CommandType.SCARD;
import static io.lettuce.core.protocol.CommandType.SDIFF;
import static io.lettuce.core.protocol.CommandType.SET;
import static io.lettuce.core.protocol.CommandType.SETEX;
import static io.lettuce.core.protocol.CommandType.SETNX;
import static io.lettuce.core.protocol.CommandType.SETRANGE;
import static io.lettuce.core.protocol.CommandType.SINTER;
import static io.lettuce.core.protocol.CommandType.SPOP;
import static io.lettuce.core.protocol.CommandType.SRANDMEMBER;
import static io.lettuce.core.protocol.CommandType.STRLEN;
import static io.lettuce.core.protocol.CommandType.SUNION;
import static io.lettuce.core.protocol.CommandType.TTL;
import static io.lettuce.core.protocol.CommandType.ZCARD;

import io.lettuce.core.GetExArgs;
import io.lettuce.core.KeyValue;
import io.lettuce.core.SetArgs;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.internal.LettuceAssert;
import io.lettuce.core.output.BooleanOutput;
import io.lettuce.core.output.DoubleOutput;
import io.lettuce.core.output.IntegerOutput;
import io.lettuce.core.output.KeyListOutput;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyStreamingOutput;
import io.lettuce.core.output.KeyValueListOutput;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingOutput;
import io.lettuce.core.output.MapOutput;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.output.ValueListOutput;
import io.lettuce.core.output.ValueOutput;
import io.lettuce.core.output.ValueSetOutput;
import io.lettuce.core.output.ValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingOutput;
import io.lettuce.core.protocol.BaseRedisCommandBuilder;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RedisCommandBuilder
 */
public class RedisCommandBuilderImpl<K, V> extends BaseRedisCommandBuilder<K, V> {

    private static final String MUST_NOT_BE_EMPTY = "must not be empty";

    private static final String MUST_NOT_BE_NULL = "must not be null";

    public static final String FIELDS ="Fields ";
    public static final String FIELD ="Field ";

    public RedisCommandBuilderImpl(RedisCodec<K, V> codec) {
        super(codec);
    }

    public Command<K, V, Long> append(K key, V value) {
        notNullKey(key);

        return createCommand(APPEND, new IntegerOutput<>(codec), key, value);
    }

    public Command<K, V, Long> decr(K key) {
        notNullKey(key);

        return createCommand(DECR, new IntegerOutput<>(codec), key);
    }

    public Command<K, V, Long> decrby(K key, long amount) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(amount);
        return createCommand(DECRBY, new IntegerOutput<>(codec), args);
    }

    public Command<K, V, Long> del(K... keys) {
        notEmpty(keys);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(DEL, new IntegerOutput<>(codec), args);
    }

    public Command<K, V, Long> del(Iterable<K> keys) {
        LettuceAssert.notNull(keys, "Keys " + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(DEL, new IntegerOutput<>(codec), args);
    }

    public Command<K, V, Long> exists(K... keys) {
        notEmpty(keys);

        return createCommand(EXISTS, new IntegerOutput<>(codec), new CommandArgs<>(codec).addKeys(keys));
    }

    public Command<K, V, Long> exists(Iterable<K> keys) {
        LettuceAssert.notNull(keys, "Keys " + MUST_NOT_BE_NULL);

        return createCommand(EXISTS, new IntegerOutput<>(codec), new CommandArgs<>(codec).addKeys(keys));
    }

    public Command<K, V, Boolean> expire(K key, long seconds) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(seconds);
        return createCommand(EXPIRE, new BooleanOutput<>(codec), args);
    }

    public Command<K, V, Boolean> expireat(K key, long timestamp) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(timestamp);
        return createCommand(EXPIREAT, new BooleanOutput<>(codec), args);
    }

    public Command<K, V, V> get(K key) {
        notNullKey(key);

        return createCommand(GET, new ValueOutput<>(codec), key);
    }

    public Command<K, V, Long> getbit(K key, long offset) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(offset);
        return createCommand(GETBIT, new IntegerOutput<>(codec), args);
    }

    public Command<K, V, V> getdel(K key) {
        notNullKey(key);

        return createCommand(GETDEL, new ValueOutput<>(codec), key);
    }

    public Command<K, V, V> getex(K key, GetExArgs getExArgs) {
        notNullKey(key);
        LettuceAssert.notNull(getExArgs, "GetExArgs " + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key);

        getExArgs.build(args);

        return createCommand(GETEX, new ValueOutput<>(codec), args);
    }

    public Command<K, V, V> getrange(K key, long start, long end) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(start).add(end);
        return createCommand(GETRANGE, new ValueOutput<>(codec), args);
    }

    public Command<K, V, V> getset(K key, V value) {
        notNullKey(key);

        return createCommand(GETSET, new ValueOutput<>(codec), key, value);
    }

    public Command<K, V, Long> hdel(K key, K... fields) {
        notNullKey(key);
        LettuceAssert.notNull(fields, FIELDS + MUST_NOT_BE_NULL);
        LettuceAssert.notEmpty(fields, FIELDS + MUST_NOT_BE_EMPTY);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addKeys(fields);
        return createCommand(HDEL, new IntegerOutput<>(codec), args);
    }

    public Command<K, V, Boolean> hexists(K key, K field) {
        notNullKey(key);
        LettuceAssert.notNull(field, FIELD + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addKey(field);
        return createCommand(HEXISTS, new BooleanOutput<>(codec), args);
    }

    public Command<K, V, V> hget(K key, K field) {
        notNullKey(key);
        LettuceAssert.notNull(field, FIELD + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addKey(field);
        return createCommand(HGET, new ValueOutput<>(codec), args);
    }

    public Command<K, V, Map<K, V>> hgetall(K key) {
        notNullKey(key);

        return createCommand(HGETALL, new MapOutput<>(codec), key);
    }

    public Command<K, V, Long> hgetall(KeyValueStreamingChannel<K, V> channel, K key) {
        notNullKey(key);
        notNull(channel);

        return createCommand(HGETALL, new KeyValueStreamingOutput<>(codec, channel), key);
    }

    public Command<K, V, Long> hincrby(K key, K field, long amount) {
        notNullKey(key);
        LettuceAssert.notNull(field, FIELD + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addKey(field).add(amount);
        return createCommand(HINCRBY, new IntegerOutput<>(codec), args);
    }

    public Command<K, V, Double> hincrbyfloat(K key, K field, double amount) {
        notNullKey(key);
        LettuceAssert.notNull(field, FIELD + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addKey(field).add(amount);
        return createCommand(HINCRBYFLOAT, new DoubleOutput<>(codec), args);
    }

    public Command<K, V, List<K>> hkeys(K key) {
        notNullKey(key);

        return createCommand(HKEYS, new KeyListOutput<>(codec), key);
    }

    public Command<K, V, Long> hkeys(KeyStreamingChannel<K> channel, K key) {
        notNullKey(key);
        notNull(channel);

        return createCommand(HKEYS, new KeyStreamingOutput<>(codec, channel), key);
    }

    public Command<K, V, Long> hlen(K key) {
        notNullKey(key);

        return createCommand(HLEN, new IntegerOutput<>(codec), key);
    }

    public Command<K, V, Long> hmget(KeyValueStreamingChannel<K, V> channel, K key, K... fields) {
        notNullKey(key);
        LettuceAssert.notNull(fields, FIELDS + MUST_NOT_BE_NULL);
        LettuceAssert.notEmpty(fields, FIELDS + MUST_NOT_BE_EMPTY);
        notNull(channel);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addKeys(fields);
        return createCommand(HMGET, new KeyValueStreamingOutput<>(codec, channel, Arrays.asList(fields)), args);
    }

    public Command<K, V, List<KeyValue<K, V>>> hmgetKeyValue(K key, K... fields) {
        notNullKey(key);
        LettuceAssert.notNull(fields, FIELDS + MUST_NOT_BE_NULL);
        LettuceAssert.notEmpty(fields, FIELDS + MUST_NOT_BE_EMPTY);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addKeys(fields);
        return createCommand(HMGET, new KeyValueListOutput<>(codec, Arrays.asList(fields)), args);
    }

    public Command<K, V, String> hmset(K key, Map<K, V> map) {
        notNullKey(key);
        LettuceAssert.notNull(map, "Map " + MUST_NOT_BE_NULL);
        LettuceAssert.isTrue(!map.isEmpty(), "Map " + MUST_NOT_BE_EMPTY);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(map);
        return createCommand(HMSET, new StatusOutput<>(codec), args);
    }

    public Command<K, V, Boolean> hset(K key, K field, V value) {
        notNullKey(key);
        LettuceAssert.notNull(field, FIELD + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addKey(field).addValue(value);
        return createCommand(HSET, new BooleanOutput<>(codec), args);
    }

    public Command<K, V, Long> hset(K key, Map<K, V> map) {
        notNullKey(key);
        LettuceAssert.notNull(map, "Map " + MUST_NOT_BE_NULL);
        LettuceAssert.isTrue(!map.isEmpty(), "Map " + MUST_NOT_BE_EMPTY);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(map);
        return createCommand(HSET, new IntegerOutput<>(codec), args);
    }

    public Command<K, V, Boolean> hsetnx(K key, K field, V value) {
        notNullKey(key);
        LettuceAssert.notNull(field, FIELD + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addKey(field).addValue(value);
        return createCommand(HSETNX, new BooleanOutput<>(codec), args);
    }

    public Command<K, V, List<V>> hvals(K key) {
        notNullKey(key);

        return createCommand(HVALS, new ValueListOutput<>(codec), key);
    }

    public Command<K, V, Long> hvals(ValueStreamingChannel<V> channel, K key) {
        notNullKey(key);
        notNull(channel);

        return createCommand(HVALS, new ValueStreamingOutput<>(codec, channel), key);
    }

    public Command<K, V, Long> incr(K key) {
        notNullKey(key);

        return createCommand(INCR, new IntegerOutput<>(codec), key);
    }

    public Command<K, V, Long> incrby(K key, long amount) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(amount);
        return createCommand(INCRBY, new IntegerOutput<>(codec), args);
    }

    public Command<K, V, Double> incrbyfloat(K key, double amount) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(amount);
        return createCommand(INCRBYFLOAT, new DoubleOutput<>(codec), args);
    }

    public Command<K, V, List<K>> keys(K pattern) {
        LettuceAssert.notNull(pattern, "Pattern " + MUST_NOT_BE_NULL);

        return createCommand(CommandType.KEYS, new KeyListOutput<>(codec), pattern);
    }

    public Command<K, V, Long> keys(KeyStreamingChannel<K> channel, K pattern) {
        LettuceAssert.notNull(pattern, "Pattern " + MUST_NOT_BE_NULL);
        notNull(channel);

        return createCommand(CommandType.KEYS, new KeyStreamingOutput<>(codec, channel), pattern);
    }

    public Command<K, V, V> lindex(K key, long index) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(index);
        return createCommand(LINDEX, new ValueOutput<>(codec), args);
    }

    public Command<K, V, Long> llen(K key) {
        notNullKey(key);

        return createCommand(LLEN, new IntegerOutput<>(codec), key);
    }

    public Command<K, V, V> lpop(K key) {
        notNullKey(key);

        return createCommand(LPOP, new ValueOutput<>(codec), key);
    }

    public Command<K, V, List<V>> lpop(K key, long count) {
        notNullKey(key);

        return createCommand(LPOP, new ValueListOutput<>(codec), new CommandArgs<>(codec).addKey(key).add(count));
    }

    public Command<K, V, List<V>> lrange(K key, long start, long stop) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(start).add(stop);
        return createCommand(LRANGE, new ValueListOutput<>(codec), args);
    }

    public Command<K, V, Long> lrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        notNullKey(key);
        notNull(channel);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(start).add(stop);
        return createCommand(LRANGE, new ValueStreamingOutput<>(codec, channel), args);
    }

    public Command<K, V, String> lset(K key, long index, V value) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(index).addValue(value);
        return createCommand(LSET, new StatusOutput<>(codec), args);
    }

    public Command<K, V, String> ltrim(K key, long start, long stop) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(start).add(stop);
        return createCommand(LTRIM, new StatusOutput<>(codec), args);
    }

    public Command<K, V, Long> mget(KeyValueStreamingChannel<K, V> channel, K... keys) {
        notEmpty(keys);
        notNull(channel);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(MGET, new KeyValueStreamingOutput<>(codec, channel, Arrays.asList(keys)), args);
    }

    public Command<K, V, Long> mget(KeyValueStreamingChannel<K, V> channel, Iterable<K> keys) {
        LettuceAssert.notNull(keys, KEYS + MUST_NOT_BE_NULL);
        notNull(channel);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(MGET, new KeyValueStreamingOutput<>(codec, channel, keys), args);
    }

    public Command<K, V, List<KeyValue<K, V>>> mgetKeyValue(K... keys) {
        notEmpty(keys);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(MGET, new KeyValueListOutput<>(codec, Arrays.asList(keys)), args);
    }

    public Command<K, V, List<KeyValue<K, V>>> mgetKeyValue(Iterable<K> keys) {
        LettuceAssert.notNull(keys, KEYS + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(MGET, new KeyValueListOutput<>(codec, keys), args);
    }

    public Command<K, V, String> mset(Map<K, V> map) {
        LettuceAssert.notNull(map, "Map " + MUST_NOT_BE_NULL);
        LettuceAssert.isTrue(!map.isEmpty(), "Map " + MUST_NOT_BE_EMPTY);

        CommandArgs<K, V> args = new CommandArgs<>(codec).add(map);
        return createCommand(MSET, new StatusOutput<>(codec), args);
    }

    public Command<K, V, Boolean> msetnx(Map<K, V> map) {
        LettuceAssert.notNull(map, "Map " + MUST_NOT_BE_NULL);
        LettuceAssert.isTrue(!map.isEmpty(), "Map " + MUST_NOT_BE_EMPTY);

        CommandArgs<K, V> args = new CommandArgs<>(codec).add(map);
        return createCommand(MSETNX, new BooleanOutput<>(codec), args);
    }

    public Command<K, V, Boolean> persist(K key) {
        notNullKey(key);

        return createCommand(PERSIST, new BooleanOutput<>(codec), key);
    }

    public Command<K, V, Boolean> pexpire(K key, long milliseconds) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(milliseconds);
        return createCommand(PEXPIRE, new BooleanOutput<>(codec), args);
    }

    public Command<K, V, Boolean> pexpireat(K key, long timestamp) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(timestamp);
        return createCommand(PEXPIREAT, new BooleanOutput<>(codec), args);
    }

    public Command<K, V, String> psetex(K key, long milliseconds, V value) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(milliseconds).addValue(value);
        return createCommand(PSETEX, new StatusOutput<>(codec), args);
    }

    public Command<K, V, Long> pttl(K key) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key);
        return createCommand(PTTL, new IntegerOutput<>(codec), args);
    }


    public Command<K, V, String> rename(K key, K newKey) {
        notNullKey(key);
        LettuceAssert.notNull(newKey, "NewKey " + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addKey(newKey);
        return createCommand(RENAME, new StatusOutput<>(codec), args);
    }

    public Command<K, V, Boolean> renamenx(K key, K newKey) {
        notNullKey(key);
        LettuceAssert.notNull(newKey, "NewKey " + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addKey(newKey);
        return createCommand(RENAMENX, new BooleanOutput<>(codec), args);
    }

    public Command<K, V, V> rpop(K key) {
        notNullKey(key);

        return createCommand(RPOP, new ValueOutput<>(codec), key);
    }

    public Command<K, V, List<V>> rpop(K key, long count) {
        notNullKey(key);

        return createCommand(RPOP, new ValueListOutput<>(codec), new CommandArgs<>(codec).addKey(key).add(count));
    }

    public Command<K, V, V> rpoplpush(K source, K destination) {
        LettuceAssert.notNull(source, "Source " + MUST_NOT_BE_NULL);
        LettuceAssert.notNull(destination, "Destination " + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(source).addKey(destination);
        return createCommand(RPOPLPUSH, new ValueOutput<>(codec), args);
    }

    public Command<K, V, Long> scard(K key) {
        notNullKey(key);

        return createCommand(SCARD, new IntegerOutput<>(codec), key);
    }

    public Command<K, V, Set<V>> sdiff(K... keys) {
        notEmpty(keys);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(SDIFF, new ValueSetOutput<>(codec), args);
    }

    public Command<K, V, Long> sdiff(ValueStreamingChannel<V> channel, K... keys) {
        notEmpty(keys);
        notNull(channel);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(SDIFF, new ValueStreamingOutput<>(codec, channel), args);
    }

    public Command<K, V, String> set(K key, V value) {
        notNullKey(key);

        return createCommand(SET, new StatusOutput<>(codec), key, value);
    }

    public Command<K, V, String> set(K key, V value, SetArgs setArgs) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addValue(value);
        setArgs.build(args);
        return createCommand(SET, new StatusOutput<>(codec), args);
    }

    public Command<K, V, V> setGet(K key, V value) {
        return setGet(key, value, new SetArgs());
    }

    public Command<K, V, V> setGet(K key, V value, SetArgs setArgs) {
        notNullKey(key);
        LettuceAssert.notNull(setArgs, "SetArgs " + MUST_NOT_BE_NULL);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).addValue(value);
        setArgs.build(args);
        args.add(GET);

        return createCommand(SET, new ValueOutput<>(codec), args);
    }

    public Command<K, V, String> setex(K key, long seconds, V value) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(seconds).addValue(value);
        return createCommand(SETEX, new StatusOutput<>(codec), args);
    }

    public Command<K, V, Boolean> setnx(K key, V value) {
        notNullKey(key);
        return createCommand(SETNX, new BooleanOutput<>(codec), key, value);
    }

    public Command<K, V, Long> setrange(K key, long offset, V value) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(offset).addValue(value);
        return createCommand(SETRANGE, new IntegerOutput<>(codec), args);
    }

    public Command<K, V, Set<V>> sinter(K... keys) {
        notEmpty(keys);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(SINTER, new ValueSetOutput<>(codec), args);
    }

    public Command<K, V, Long> sinter(ValueStreamingChannel<V> channel, K... keys) {
        notEmpty(keys);
        notNull(channel);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(SINTER, new ValueStreamingOutput<>(codec, channel), args);
    }

    public Command<K, V, V> spop(K key) {
        notNullKey(key);

        return createCommand(SPOP, new ValueOutput<>(codec), key);
    }

    public Command<K, V, Set<V>> spop(K key, long count) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(count);
        return createCommand(SPOP, new ValueSetOutput<>(codec), args);
    }

    public Command<K, V, V> srandmember(K key) {
        notNullKey(key);

        return createCommand(SRANDMEMBER, new ValueOutput<>(codec), key);
    }

    public Command<K, V, List<V>> srandmember(K key, long count) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(count);
        return createCommand(SRANDMEMBER, new ValueListOutput<>(codec), args);
    }

    public Command<K, V, Long> srandmember(ValueStreamingChannel<V> channel, K key, long count) {
        notNullKey(key);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKey(key).add(count);
        return createCommand(SRANDMEMBER, new ValueStreamingOutput<>(codec, channel), args);
    }

    public Command<K, V, Long> strlen(K key) {
        notNullKey(key);

        return createCommand(STRLEN, new IntegerOutput<>(codec), key);
    }

    public Command<K, V, Set<V>> sunion(K... keys) {
        notEmpty(keys);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(SUNION, new ValueSetOutput<>(codec), args);
    }

    public Command<K, V, Long> sunion(ValueStreamingChannel<V> channel, K... keys) {
        notEmpty(keys);
        notNull(channel);

        CommandArgs<K, V> args = new CommandArgs<>(codec).addKeys(keys);
        return createCommand(SUNION, new ValueStreamingOutput<>(codec, channel), args);
    }

    public Command<K, V, Long> ttl(K key) {
        notNullKey(key);

        return createCommand(TTL, new IntegerOutput<>(codec), key);
    }

    public Command<K, V, String> type(K key) {
        notNullKey(key);

        return createCommand(CommandType.TYPE, new StatusOutput<>(codec), key);
    }

    public Command<K, V, Long> zcard(K key) {
        notNullKey(key);

        return createCommand(ZCARD, new IntegerOutput<>(codec), key);
    }

    public static void notNull(KeyStreamingChannel<?> channel) {
        LettuceAssert.notNull(channel, "KeyValueStreamingChannel " + MUST_NOT_BE_NULL);
    }

    public static void notNull(ValueStreamingChannel<?> channel) {
        LettuceAssert.notNull(channel, "ValueStreamingChannel " + MUST_NOT_BE_NULL);
    }

    public static void notNull(KeyValueStreamingChannel<?, ?> channel) {
        LettuceAssert.notNull(channel, "KeyValueStreamingChannel " + MUST_NOT_BE_NULL);
    }

    private static void notEmpty(Object[] keys) {
        LettuceAssert.notNull(keys, KEYS + MUST_NOT_BE_NULL);
        LettuceAssert.notEmpty(keys, KEYS + MUST_NOT_BE_EMPTY);
    }

    private static void notNullKey(Object key) {
        LettuceAssert.notNull(key, "Key " + MUST_NOT_BE_NULL);
    }

}
