package io.arex.inst.jedis.v4;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.inst.jedis.common.JedisExtractor;
import io.arex.inst.jedis.common.RedisKeyUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisSocketFactory;
import redis.clients.jedis.params.SetParams;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class JedisWrapper extends Jedis {
    private final String url;

    public JedisWrapper(JedisSocketFactory jedisSocketFactory, JedisClientConfig clientConfig) {
        super(jedisSocketFactory, clientConfig);
        this.url = this.connection.toString();
    }

    public String set(String key, String value) {
        return this.call("set", key, value, () -> super.set(key, value));
    }

    public String set(String key, String value, SetParams params) {
        return this.call("set", key, value, () -> super.set(key, value, params));
    }

    public String get(String key) {
        return call("get", key, () -> super.get(key));
    }

    public long exists(String... keys) {
        return call("exists", RedisKeyUtil.generate(keys), () -> super.exists(keys));
    }

    public boolean exists(String key) {
        return call("exists", key, () -> super.exists(key));
    }

    public long del(String... keys) {
        return call("del", RedisKeyUtil.generate(keys), () -> super.del(keys));
    }

    public long del(String key) {
        return call("del", key, () -> super.del(key));
    }

    public String type(String key) {
        return call("type", key, () -> super.type(key));
    }

    public Set<String> keys(String pattern) {
        return call("keys", pattern, () -> super.keys(pattern));
    }

    public long expire(String key, int seconds) {
        return call("expire", key, () -> super.expire(key, seconds));
    }

    public long expireAt(String key, long unixTime) {
        return call("expireAt", key, () -> super.expireAt(key, unixTime));
    }

    public long ttl(String key) {
        return call("ttl", key, () -> super.ttl(key));
    }

    public String getSet(String key, String value) {
        return call("getSet", key, value, () -> super.getSet(key, value));
    }

    public List<String> mget(String... keys) {
        return call("mget", RedisKeyUtil.generate(keys), () -> super.mget(keys));
    }

    public long setnx(String key, String value) {
        return call("setnx", key, value, () -> super.setnx(key, value));
    }

    public String setex(String key, int seconds, String value) {
        return call("setex", key, value, () -> super.setex(key, seconds, value));
    }

    public String mset(String... keysvalues) {
        return call("mset", keysvalues, () -> super.mset(keysvalues));
    }

    public long msetnx(String... keysvalues) {
        return call("msetnx", keysvalues, () -> super.msetnx(keysvalues));
    }

    public long decrBy(String key, long integer) {
        return call("decrBy", RedisKeyUtil.generate(key, String.valueOf(integer)), () -> super.decrBy(key, integer));
    }

    public long decr(String key) {
        return call("decr", key, () -> super.decr(key));
    }

    public long incrBy(String key, long integer) {
        return call("incrBy", RedisKeyUtil.generate(key, String.valueOf(integer)),
                () -> super.incrBy(key, integer));
    }

    public double incrByFloat(String key, double value) {
        return call("incrByFloat", key, () -> super.incrByFloat(key, value));
    }

    public long incr(String key) {
        return call("incr", key, () -> super.incr(key));
    }

    public long append(String key, String value) {
        return call("append", key, value, () -> super.append(key, value));
    }

    public String substr(String key, int start, int end) {
        return call("substr", RedisKeyUtil.generate(key, String.valueOf(start) + String.valueOf(end)),
                () -> super.substr(key, start, end));
    }

    public long hset(String key, String field, String value) {
        return call("hset", RedisKeyUtil.generate(key, field), value,
                () -> super.hset(key, field, value));
    }

    public String hget(String key, String field) {
        return call("hget", RedisKeyUtil.generate(key, field), () -> super.hget(key, field));
    }

    public long hsetnx(String key, String field, String value) {
        return call("hsetnx", RedisKeyUtil.generate(key, field), value,
                () -> super.hsetnx(key, field, value));
    }

    public String hmset(String key, Map<String, String> hash) {
        return call("hmset", key, SerializeUtils.serialize(hash), () -> super.hmset(key, hash));
    }

    public List<String> hmget(String key, String... fields) {
        return call("hmget", RedisKeyUtil.generate(key, fields), () -> super.hmget(key, fields));
    }

    public long hincrBy(String key, String field, long value) {
        return call("hincrBy", RedisKeyUtil.generate(key, field), String.valueOf(value),
                () -> super.hincrBy(key, field, value));
    }

    public double hincrByFloat(String key, String field, double value) {
        return call("hincrByFloat", RedisKeyUtil.generate(key, field), String.valueOf(value),
                () -> super.hincrByFloat(key, field, value));
    }

    public boolean hexists(String key, String field) {
        return call("hexists", RedisKeyUtil.generate(key, field), () -> super.hexists(key, field));
    }

    public long hdel(String key, String... fields) {
        return call("hdel", RedisKeyUtil.generate(key, fields), () -> super.hdel(key, fields));
    }

    public long hlen(String key) {
        return call("hlen", key, () -> super.hlen(key));
    }

    public Set<String> hkeys(String key) {
        return call("hkeys", key, () -> super.hkeys(key));
    }

    public List<String> hvals(String key) {
        return call("hvals", key, () -> super.hvals(key));
    }

    public Map<String, String> hgetAll(String key) {
        return call("hgetAll", key, () -> super.hgetAll(key));
    }

    public long llen(String key) {
        return call("llen", key, () -> super.llen(key));
    }

    public List<String> lrange(String key, long start, long end) {
        return call("lrange", RedisKeyUtil.generate(key, String.valueOf(start) + end),
                () -> super.lrange(key, start, end));
    }

    public String ltrim(String key, long start, long end) {
        return call("ltrim", RedisKeyUtil.generate(key, String.valueOf(start) + end),
                () -> super.ltrim(key, start, end));
    }

    public String lindex(String key, long index) {
        return call("lindex", RedisKeyUtil.generate(key, String.valueOf(index)),
                () -> super.lindex(key, index));
    }

    public String lset(String key, long index, String value) {
        return call("lset", RedisKeyUtil.generate(key, String.valueOf(index)), value,
                () -> super.lset(key, index, value));
    }

    public String lpop(String key) {
        return call("lpop", key, () -> super.lpop(key));
    }

    public String rpop(String key) {
        return call("rpop", key, () -> super.rpop(key));
    }

    public String spop(String key) {
        return call("spop", key, () -> super.spop(key));
    }

    public Set<String> spop(String key, long count) {
        return call("spop", RedisKeyUtil.generate(key, String.valueOf(count)),
                () -> super.spop(key, count));
    }

    public long scard(String key) {
        return call("scard", key, () -> super.scard(key));
    }

    public Set<String> sinter(String... keys) {
        return call("sinter", RedisKeyUtil.generate(keys), () -> super.sinter(keys));
    }

    public Set<String> sunion(String... keys) {
        return call("sunion", RedisKeyUtil.generate(keys), () -> super.sunion(keys));
    }

    public Set<String> sdiff(String... keys) {
        return call("sdiff", RedisKeyUtil.generate(keys), () -> super.sdiff(keys));
    }

    public String srandmember(String key) {
        return call("srandmember", key, () -> super.srandmember(key));
    }

    public List<String> srandmember(String key, int count) {
        return call("", RedisKeyUtil.generate(key, String.valueOf(count)),
                () -> super.srandmember(key, count));
    }

    public long zcard(String key) {
        return call("zcard", key, () -> super.zcard(key));
    }

    public long strlen(String key) {
        return call("strlen", key, () -> super.strlen(key));
    }

    public long persist(String key) {
        return call("persist", key, () -> super.persist(key));
    }

    public long setrange(String key, long offset, String value) {
        return call("setrange", RedisKeyUtil.generate(key, String.valueOf(offset)), value,
                () -> super.setrange(key, offset, value));
    }

    public String getrange(String key, long startOffset, long endOffset) {
        return call("getrange", RedisKeyUtil.generate(key, String.valueOf(startOffset) + endOffset),
                () -> super.getrange(key, startOffset, endOffset));
    }

    public long pexpire(String key, long milliseconds) {
        return call("pexpire", key, () -> super.pexpire(key, milliseconds));
    }

    public long pexpireAt(String key, long millisecondsTimestamp) {
        return call("", key, () -> super.pexpireAt(key, millisecondsTimestamp));
    }

    public long pttl(String key) {
        return call("pttl", key, () -> super.pttl(key));
    }

    public String psetex(String key, long milliseconds, String value) {
        return call("psetex", key, value, () -> super.psetex(key, milliseconds, value));
    }

    @Override
    public String set(final byte[] key, final byte[] value) {
        return call("set", Base64.getEncoder().encodeToString(key),
                Base64.getEncoder().encodeToString(value), () -> super.set(key, value));
    }

    @Override
    public String set(final byte[] key, final byte[] value, final SetParams params) {
        return call("set", Base64.getEncoder().encodeToString(key),
                Base64.getEncoder().encodeToString(value), () -> super.set(key, value, params));
    }

    @Override
    public byte[] get(final byte[] key) {
        return call("get", Base64.getEncoder().encodeToString(key), () -> super.get(key));
    }

    @Override
    public long exists(final byte[]... keys) {
        return call("exists", RedisKeyUtil.generate(keys), () -> super.exists(keys));
    }

    @Override
    public boolean exists(final byte[] key) {
        return call("exists", Base64.getEncoder().encodeToString(key), () -> super.exists(key));
    }

    @Override
    public String type(final byte[] key) {
        return call("type", Base64.getEncoder().encodeToString(key), () -> super.type(key));
    }

    @Override
    public byte[] getSet(final byte[] key, final byte[] value) {
        return call("getSet", Base64.getEncoder().encodeToString(key),
                Base64.getEncoder().encodeToString(value), () -> super.getSet(key, value));
    }

    @Override
    public List<byte[]> mget(final byte[]... keys) {
        return call("mget", RedisKeyUtil.generate(keys), () -> super.mget(keys));
    }

    @Override
    public long setnx(final byte[] key, final byte[] value) {
        return call("setnx", String.valueOf(key),
                Base64.getEncoder().encodeToString(value), () -> super.setnx(key, value));
    }

    @Override
    public String setex(final byte[] key, final long seconds, final byte[] value) {
        return call("setex", String.valueOf(key),
                Base64.getEncoder().encodeToString(value), () -> super.setex(key, seconds, value));
    }

    private <U> U call(String methodName, String[] keysValues, Callable<U> callable) {
        if (keysValues == null || keysValues.length == 0) {
            return null;
        }

        if (keysValues.length == 2) {
            return call(keysValues[0], keysValues[1], callable);
        }

        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        for (int i = 0; i < keysValues.length; i++) {
            if (i % 2 == 0) {
                keyBuilder.append(keysValues[i]).append(',');
            } else {
                valueBuilder.append(keysValues[i]).append(',');
            }
        }

        return call(methodName, keyBuilder.toString(), valueBuilder.toString(), callable);
    }

    private <U> U call(String methodName, String key, Callable<U> callable) {
        return call(methodName, key, null, callable);
    }

    private <U> U call(String methodName, String key, String value, Callable<U> callable) {
        JedisExtractor extractor;
        if (ContextManager.needReplay()) {
            extractor = new JedisExtractor(this.url, methodName, key, value);
            return (U) extractor.replay();
        }

        U result = null;
        try {
            result = callable.call();
        } catch (Exception e) {
            return null;
        }

        if (ContextManager.needRecord()) {
            extractor = new JedisExtractor(this.url, methodName, key, value);
            extractor.record(result);
        }
        return result;
    }
}
