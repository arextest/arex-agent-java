package io.arex.inst.jedis.v3;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.redis.common.RedisKeyUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.serializer.Serializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.SetParams;

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
    public String set(String key, String value, SetParams params) {
        return this.call("set", key, params.toString(), () -> super.set(key, value, params), null);
    }

    @Override
    public String get(String key) {
        return call("get", key, () -> super.get(key), null);
    }

    @Override
    public String getDel(String key) {
        return call("getDel", key, () -> super.getDel(key), null);
    }

    @Override
    public String getEx(String key, GetExParams params) {
        return call("getEx", key, params.toString(), () -> super.getEx(key, params), null);
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
    public Long unlink(String... keys) {
        return call("unlink", RedisKeyUtil.generate(keys), () -> super.unlink(keys), 0L);
    }

    @Override
    public Long unlink(String key) {
        return call("unlink", key, () -> super.unlink(key), 0L);
    }

    @Override
    public String type(String key) {
        return call("type", key, () -> super.type(key), "none");
    }

    @Override
    public Set<String> keys(String pattern) {
        return call("keys", pattern, () -> super.keys(pattern), Collections.EMPTY_SET);
    }

    @Override
    public String rename(byte[] oldkey, byte[] newkey) {
        return call("rename", RedisKeyUtil.generate(oldkey, newkey), () -> super.rename(oldkey, newkey), null);
    }

    @Override
    public Long renamenx(byte[] oldkey, byte[] newkey) {
        return call("renamenx", RedisKeyUtil.generate(oldkey, newkey), () -> super.renamenx(oldkey, newkey), 0L);
    }

    @Override
    public Long expire(String key, long seconds) {
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
        return call("mget", RedisKeyUtil.generate(keys), () -> super.mget(keys), Collections.EMPTY_LIST);
    }

    @Override
    public Long setnx(String key, String value) {
        return call("setnx", key, () -> super.setnx(key, value), 0L);
    }

    //@Override
    //public String setex(String key, long seconds, String value) {
    //    return call("setex", key, () -> super.setex(key, seconds, value), null);
    //}

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
        return call("hset", key, hash.keySet(), () -> super.hset(key, hash), 0L);
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
        return call("hmset", key, Serializer.serialize(hash.keySet()), () -> super.hmset(key, hash),
                null);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return call("hmget", key, RedisKeyUtil.generate(fields), () -> super.hmget(key, fields),
                Collections.EMPTY_LIST);
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
        return call("hkeys", key, () -> super.hkeys(key), Collections.EMPTY_SET);
    }

    @Override
    public List<String> hvals(String key) {
        return call("hvals", key, () -> super.hvals(key), Collections.EMPTY_LIST);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return call("hgetAll", key, () -> super.hgetAll(key), Collections.EMPTY_MAP);
    }

    @Override
    public Long llen(String key) {
        return call("llen", key, () -> super.llen(key), 0L);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        return call("lrange", key, RedisKeyUtil.generate("start", String.valueOf(start), "end", String.valueOf(end)),
                () -> super.lrange(key, start, end), Collections.EMPTY_LIST);
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
                Collections.EMPTY_SET);
    }

    @Override
    public Long scard(String key) {
        return call("scard", key, () -> super.scard(key), 0L);
    }

    @Override
    public Set<String> sinter(String... keys) {
        return call("sinter", RedisKeyUtil.generate(keys), () -> super.sinter(keys), Collections.EMPTY_SET);
    }

    @Override
    public Set<String> sunion(String... keys) {
        return call("sunion", RedisKeyUtil.generate(keys), () -> super.sunion(keys), Collections.EMPTY_SET);
    }

    @Override
    public Set<String> sdiff(String... keys) {
        return call("sdiff", RedisKeyUtil.generate(keys), () -> super.sdiff(keys), Collections.EMPTY_SET);
    }

    @Override
    public String srandmember(String key) {
        return call("srandmember", key, () -> super.srandmember(key), null);
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return call("srandmember", key, RedisKeyUtil.generate("count", String.valueOf(count)),
                () -> super.srandmember(key, count), Collections.EMPTY_LIST);
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
    public Long pttl(String key) {
        return call("pttl", key, () -> super.pttl(key), 0L);
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return call("psetex", key, value, () -> super.psetex(key, milliseconds, value), null);
    }

    @Override
    public byte[] substr(byte[] key, int start, int end) {
        return call("substr", key,
                RedisKeyUtil.generate("start", String.valueOf(start), "end", String.valueOf(end)),
                () -> super.substr(key, start, end), null);
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        return call("hset", key, field, () -> super.hset(key, field, value), 0L);
    }

    @Override
    public Long hset(byte[] key, Map<byte[], byte[]> hash) {
        return call("hset", key, hash.keySet(), () -> super.hset(key, hash), 0L);
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return call("hget", key, field, () -> super.hget(key, field), null);
    }

    @Override
    public Long hdel(byte[] key, byte[]... fields) {
        return call("hdel", key, RedisKeyUtil.generate(fields), () -> super.hdel(key, fields), 0L);
    }

    @Override
    public List<byte[]> hvals(byte[] key) {
        return call("hvals", key, () -> super.hvals(key), Collections.EMPTY_LIST);
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        return call("hgetAll", key, () -> super.hgetAll(key), Collections.EMPTY_MAP);
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
    public byte[] get(final byte[] key) {
        return call("get", key, () -> super.get(key), null);
    }

    @Override
    public Long exists(final byte[]... keys) {
        return call("exists", RedisKeyUtil.generate(keys), () -> super.exists(keys), 0L);
    }

    @Override
    public Boolean exists(final byte[] key) {
        return call("exists", key, () -> super.exists(key), false);
    }

    @Override
    public String type(final byte[] key) {
        return call("type", key, () -> super.type(key), "none");
    }

    @Override
    public byte[] getSet(final byte[] key, final byte[] value) {
        return call("getSet", key, () -> super.getSet(key, value), null);
    }

    @Override
    public List<byte[]> mget(final byte[]... keys) {
        return call("mget", RedisKeyUtil.generate(keys), () -> super.mget(keys), Collections.EMPTY_LIST);
    }

    @Override
    public Long setnx(final byte[] key, final byte[] value) {
        return call("setnx", key, () -> super.setnx(key, value), 0L);
    }

    @Override
    public String setex(byte[] key, int seconds, byte[] value) {
        return call("setex", key, () -> super.setex(key, seconds, value),
                null);
    }

    @Override
    public Long unlink(byte[]... keys) {
        return call("unlink", RedisKeyUtil.generate(keys), () -> super.unlink(keys), 0L);
    }

    @Override
    public Long unlink(byte[] key) {
        return call("unlink", key, () -> super.unlink(key), 0L);
    }

    @Override
    public String rename(String oldkey, String newkey) {
        return call("rename", RedisKeyUtil.generate(oldkey, newkey), () -> super.rename(oldkey, newkey), null);
    }

    @Override
    public Long renamenx(String oldkey, String newkey) {
        return call("renamenx", RedisKeyUtil.generate(oldkey, newkey), () -> super.renamenx(oldkey, newkey), 0L);
    }

    @Override
    public String ping() {
        return call("ping", "", () -> super.ping(), null);
    }

    @Override
    public byte[] ping(byte[] message) {
        return call("ping", message, () -> super.ping(message), null);
    }

    @Override
    public String ping(String message) {
        return call("ping", message, () -> super.ping(message), null);
    }

    /**
     * mset/msetnx
     */
    private <U> U call(String command, String[] keysValues, Callable<U> callable, U defaultValue) {
        if (ArrayUtils.isEmpty(keysValues)) {
            return defaultValue;
        }

        if (keysValues.length == 2) {
            return call(command, keysValues[0], null, callable, defaultValue);
        }

        StringBuilder keyBuilder = new StringBuilder(keysValues[0]);
        for (int i = 2; i < keysValues.length; i++) {
            if (i % 2 == 0) {
                keyBuilder.append(';').append(keysValues[i]);
            }
        }

        return call(command, keyBuilder.toString(), null, callable, defaultValue);
    }

    private <U> U call(String command, Object key, Callable<U> callable, U defaultValue) {
        return call(command, key, null, callable, defaultValue);
    }

    private <U> U call(String command, Object key, Object field, Callable<U> callable, U defaultValue) {
        if (ContextManager.needRecord()) {
            RepeatedCollectManager.enter();
        }
        if (ContextManager.needReplay()) {
            RedisExtractor extractor = new RedisExtractor(this.url, command, key, field);
            MockResult mockResult = extractor.replay();
            if (mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() instanceof RuntimeException) {
                    throw (RuntimeException) mockResult.getThrowable();
                }
                return mockResult.getResult() == null ? defaultValue : (U) mockResult.getResult();
            }
        }

        U result;
        try {
            result = callable.call();
        } catch (Exception e) {
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
                RedisExtractor extractor = new RedisExtractor(this.url, command, key, field);
                extractor.record(e);
            }

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            return defaultValue;
        }

        if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
            RedisExtractor extractor = new RedisExtractor(this.url, command, key, field);
            extractor.record(result);
        }
        return result;
    }
}
