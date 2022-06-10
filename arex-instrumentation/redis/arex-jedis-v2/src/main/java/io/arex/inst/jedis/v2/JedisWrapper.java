package io.arex.inst.jedis.v2;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.inst.jedis.common.RedisExtractor;
import io.arex.inst.jedis.common.RedisKeyUtil;
import redis.clients.jedis.Jedis;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.util.*;
import java.util.concurrent.Callable;

public class JedisWrapper extends Jedis {
    private final String url;

    public JedisWrapper(final String host, final int port, final int connectionTimeout, final int soTimeout,
                        final boolean ssl, final SSLSocketFactory sslSocketFactory, final SSLParameters sslParameters,
                        final HostnameVerifier hostnameVerifier) {
        super(host, port, connectionTimeout, soTimeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
        this.url = host + ":" + port;
    }

    public JedisWrapper(String host, int port, int timeout) {
        super(host, port, timeout);
        this.url = host + ":" + port;
    }

    @Override
    public String set(String key, String value) {
        return this.call("set", key, () -> super.set(key, value), null);
    }

    @Override
    public String set(String key, String value, String expx, long time) {
        return this.call("set", key, () -> super.set(key, value, expx, time), null);
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        return this.call("set", key, () -> super.set(key, value, nxxx, expx, time), null);
    }

    @Override
    public String get(String key) {
        return call("get", key, () -> super.get(key), null);
    }

    @Override
    public Long exists(String... keys) {
        return call("exists", RedisKeyUtil.generate(keys), () -> super.exists(keys), 0L);
    }

    @Override
    public Boolean exists(String key) {
        return call("exists", key, () -> super.exists(key), false);
    }

    @Override
    public Long del(String... keys) {
        return call("del", RedisKeyUtil.generate(keys), () -> super.del(keys), 0L);
    }

    @Override
    public Long del(String key) {
        return call("del", key, () -> super.del(key), 0L);
    }

    @Override
    public String type(String key) {
        return call("type", key, () -> super.type(key), "none");
    }

    @Override
    public Set<String> keys(String pattern) {
        return call("keys", pattern, () -> super.keys(pattern), Collections.emptySet());
    }

    @Override
    public Long expire(String key, int seconds) {
        return call("expire", key, () -> super.expire(key, seconds), 0L);
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return call("expireAt", key, () -> super.expireAt(key, unixTime), 0L);
    }

    @Override
    public Long ttl(String key) {
        return call("ttl", key, () -> super.ttl(key), -1L);
    }

    @Override
    public String getSet(String key, String value) {
        return call("getSet", key, () -> super.getSet(key, value), null);
    }

    @Override
    public List<String> mget(String... keys) {
        return call("mget", RedisKeyUtil.generate(keys), () -> super.mget(keys), Collections.emptyList());
    }

    @Override
    public Long setnx(String key, String value) {
        return call("setnx", key, () -> super.setnx(key, value), 0L);
    }

    @Override
    public String setex(String key, int seconds, String value) {
        return call("setex", key, () -> super.setex(key, seconds, value), null);
    }

    @Override
    public String mset(String... keysvalues) {
        return call("mset", keysvalues, () -> super.mset(keysvalues), null);
    }

    @Override
    public Long msetnx(String... keysvalues) {
        return call("msetnx", keysvalues, () -> super.msetnx(keysvalues), 0L);
    }

    @Override
    public Long decrBy(String key, long integer) {
        return call("decrBy", key, () -> super.decrBy(key, integer), 0L);
    }

    @Override
    public Long decr(String key) {
        return call("decr", key, () -> super.decr(key), 0L);
    }

    @Override
    public Long incrBy(String key, long integer) {
        return call("incrBy", key, () -> super.incrBy(key, integer), 0L);
    }

    @Override
    public Double incrByFloat(String key, double value) {
        return call("incrByFloat", key, () -> super.incrByFloat(key, value), 0d);
    }

    @Override
    public Long incr(String key) {
        return call("incr", key, () -> super.incr(key), 0L);
    }

    @Override
    public Long append(String key, String value) {
        return call("append", key, () -> super.append(key, value), 0L);
    }

    @Override
    public String substr(String key, int start, int end) {
        return call("substr", key, RedisKeyUtil.generate("start", String.valueOf(start), "end", String.valueOf(end)),
                () -> super.substr(key, start, end), null);
    }

    @Override
    public Long hset(String key, String field, String value) {
        return call("hset", key, field, () -> super.hset(key, field, value), 0L);
    }

    @Override
    public Long hset(final String key, final Map<String, String> hash) {
        return call("hset", key, SerializeUtils.serialize(hash.keySet()), () -> super.hset(key, hash), 0L);
    }

    @Override
    public String hget(String key, String field) {
        return call("hget", key, field, () -> super.hget(key, field), null);
    }

    @Override
    public Long hsetnx(String key, String field, String value) {
        return call("hsetnx", key, field, () -> super.hsetnx(key, field, value), 0L);
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return call("hmset", key, SerializeUtils.serialize(hash.keySet()), () -> super.hmset(key, hash),
                null);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return call("hmget", key, RedisKeyUtil.generate(fields), () -> super.hmget(key, fields),
                Collections.emptyList());
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return call("hincrBy", key, field, () -> super.hincrBy(key, field, value), 0L);
    }

    @Override
    public Double hincrByFloat(String key, String field, double value) {
        return call("hincrByFloat", key, field, () -> super.hincrByFloat(key, field, value), 0d);
    }

    @Override
    public Boolean hexists(String key, String field) {
        return call("hexists", key, field, () -> super.hexists(key, field), false);
    }

    @Override
    public Long hdel(String key, String... fields) {
        return call("hdel", key, RedisKeyUtil.generate(fields), () -> super.hdel(key, fields), 0L);
    }

    @Override
    public Long hlen(String key) {
        return call("hlen", key, () -> super.hlen(key), 0L);
    }

    @Override
    public Set<String> hkeys(String key) {
        return call("hkeys", key, () -> super.hkeys(key), Collections.emptySet());
    }

    @Override
    public List<String> hvals(String key) {
        return call("hvals", key, () -> super.hvals(key), Collections.emptyList());
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return call("hgetAll", key, () -> super.hgetAll(key), Collections.emptyMap());
    }

    @Override
    public Long llen(String key) {
        return call("llen", key, () -> super.llen(key), 0L);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        return call("lrange", key, RedisKeyUtil.generate("start", String.valueOf(start), "end", String.valueOf(end)),
                () -> super.lrange(key, start, end), Collections.emptyList());
    }

    @Override
    public String ltrim(String key, long start, long end) {
        return call("ltrim", key, RedisKeyUtil.generate("start", String.valueOf(start), "end", String.valueOf(end)),
                () -> super.ltrim(key, start, end), null);
    }

    @Override
    public String lindex(String key, long index) {
        return call("lindex", key, RedisKeyUtil.generate("index", String.valueOf(index)),
                () -> super.lindex(key, index), null);
    }

    @Override
    public String lset(String key, long index, String value) {
        return call("lset", key, RedisKeyUtil.generate("index", String.valueOf(index)),
                () -> super.lset(key, index, value), null);
    }

    @Override
    public String lpop(String key) {
        return call("lpop", key, () -> super.lpop(key), null);
    }

    @Override
    public String rpop(String key) {
        return call("rpop", key, () -> super.rpop(key), null);
    }

    @Override
    public String spop(String key) {
        return call("spop", key, () -> super.spop(key), null);
    }

    @Override
    public Set<String> spop(String key, long count) {
        return call("spop", RedisKeyUtil.generate(key, String.valueOf(count)), () -> super.spop(key, count),
                Collections.emptySet());
    }

    @Override
    public Long scard(String key) {
        return call("scard", key, () -> super.scard(key), 0L);
    }

    @Override
    public Set<String> sinter(String... keys) {
        return call("sinter", RedisKeyUtil.generate(keys), () -> super.sinter(keys), Collections.emptySet());
    }

    @Override
    public Set<String> sunion(String... keys) {
        return call("sunion", RedisKeyUtil.generate(keys), () -> super.sunion(keys), Collections.emptySet());
    }

    @Override
    public Set<String> sdiff(String... keys) {
        return call("sdiff", RedisKeyUtil.generate(keys), () -> super.sdiff(keys), Collections.emptySet());
    }

    @Override
    public String srandmember(String key) {
        return call("srandmember", key, () -> super.srandmember(key), null);
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return call("srandmember", key, RedisKeyUtil.generate("count", String.valueOf(count)),
                () -> super.srandmember(key, count), Collections.emptyList());
    }

    @Override
    public Long zcard(String key) {
        return call("zcard", key, () -> super.zcard(key), 0L);
    }

    @Override
    public Long strlen(String key) {
        return call("strlen", key, () -> super.strlen(key), 0L);
    }

    @Override
    public Long persist(String key) {
        return call("persist", key, () -> super.persist(key), 0L);
    }

    @Override
    public Long setrange(String key, long offset, String value) {
        return call("setrange", key, RedisKeyUtil.generate("offset", String.valueOf(offset)),
                () -> super.setrange(key, offset, value), 0L);
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return call("getrange", key, RedisKeyUtil.generate(
                        RedisKeyUtil.generate("startOffset", String.valueOf(startOffset), "endOffset", String.valueOf(endOffset))),
                () -> super.getrange(key, startOffset, endOffset), null);
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        return call("pexpire", key, () -> super.pexpire(key, milliseconds), 0L);
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return call("pexpireAt", key, () -> super.pexpireAt(key, millisecondsTimestamp), 0L);
    }

    @Override
    public Long pttl(String key) {
        return call("pttl", key, () -> super.pttl(key), 0L);
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return call("psetex", key, value, () -> super.psetex(key, milliseconds, value), null);
    }

    @Override
    public String set(final byte[] key, final byte[] value) {
        return call("set", Base64.getEncoder().encodeToString(key), () -> super.set(key, value), null);
    }

    @Override
    public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time) {
        return call("set", Base64.getEncoder().encodeToString(key), () -> super.set(key, value, nxxx, expx, time), null);
    }

    @Override
    public String set(byte[] key, byte[] value, byte[] expx, long time) {
        return call("set", Base64.getEncoder().encodeToString(key), () -> super.set(key, value, expx, time), null);
    }

    @Override
    public byte[] get(final byte[] key) {
        return call("get", Base64.getEncoder().encodeToString(key), () -> super.get(key), null);
    }

    @Override
    public Long exists(final byte[]... keys) {
        return call("exists", RedisKeyUtil.generate(keys), () -> super.exists(keys), 0L);
    }

    @Override
    public Boolean exists(final byte[] key) {
        return call("exists", Base64.getEncoder().encodeToString(key), () -> super.exists(key), false);
    }

    @Override
    public String type(final byte[] key) {
        return call("type", Base64.getEncoder().encodeToString(key), () -> super.type(key), "none");
    }

    @Override
    public byte[] getSet(final byte[] key, final byte[] value) {
        return call("getSet", Base64.getEncoder().encodeToString(key), () -> super.getSet(key, value), null);
    }

    @Override
    public List<byte[]> mget(final byte[]... keys) {
        return call("mget", RedisKeyUtil.generate(keys), () -> super.mget(keys), Collections.emptyList());
    }

    @Override
    public Long setnx(final byte[] key, final byte[] value) {
        return call("setnx", Base64.getEncoder().encodeToString(key), () -> super.setnx(key, value), 0L);
    }

    @Override
    public String setex(byte[] key, int seconds, byte[] value) {
        return call("setex", Base64.getEncoder().encodeToString(key), () -> super.setex(key, seconds, value),
                null);
    }

    /**
     * mset/msetnx
     */
    private <U> U call(String command, String[] keysValues, Callable<U> callable, U defaultValue) {
        if (keysValues == null || keysValues.length == 0) {
            return null;
        }

        if (keysValues.length == 2) {
            return call(command, keysValues[0], null, callable, defaultValue);
        }

        StringBuilder keyBuilder = new StringBuilder();

        keyBuilder.append(keysValues[0]);
        for (int i = 2; i < keysValues.length; i++) {
            if (i % 2 == 0) {
                keyBuilder.append(';').append(keysValues[i]);
            }
        }

        return call(command, keyBuilder.toString(), null, callable, defaultValue);
    }

    private <U> U call(String command, String key, Callable<U> callable, U defaultValue) {
        return call(command, key, null, callable, defaultValue);
    }

    private <U> U call(String command, String key, String field, Callable<U> callable, U defaultValue) {
        RedisExtractor extractor;
        if (ContextManager.needReplay()) {
            extractor = new RedisExtractor(this.url, command, key, field);
            Object replayResult = extractor.replay();
            return replayResult == null ? defaultValue : (U) replayResult;
        }
        
        U result;
        try {
            result = callable.call();
        } catch (Exception e) {
            if (!ContextManager.needRecord()) {
                extractor = new RedisExtractor(this.url, command, key, field);
                extractor.record(e);
            }
            return defaultValue;
        }

        if (ContextManager.needRecord()) {
            extractor = new RedisExtractor(this.url, command, key, field);
            extractor.record(result);
        }
        return result;
    }
}
