package io.arex.inst.redisson.v3.wrapper;

import io.arex.inst.redis.common.RedisKeyUtil;
import io.arex.inst.redisson.v3.RedissonWrapperCommon;
import io.arex.inst.redisson.v3.util.RedisUtil;
import org.redisson.RedissonMap;
import org.redisson.WriteBehindService;
import org.redisson.api.MapOptions;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.ConnectionManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RedissonMapWrapper
 */
public class RedissonMapWrapper<K, V> extends RedissonMap<K, V> {
    private final String redisUri;

    public RedissonMapWrapper(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson,
        MapOptions<K, V> options, WriteBehindService writeBehindService) {
        super(commandExecutor, name, redisson, options, writeBehindService);
        ConnectionManager connectionManager = commandExecutor.getConnectionManager();
        redisUri = RedisUtil.getRedisUri(connectionManager);    }

    public RedissonMapWrapper(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson,
        MapOptions<K, V> options, WriteBehindService writeBehindService) {
        super(codec, commandExecutor, name, redisson, options, writeBehindService);
        ConnectionManager connectionManager = commandExecutor.getConnectionManager();
        redisUri = RedisUtil.getRedisUri(connectionManager);    }

    @Override
    public RFuture<Integer> sizeAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HLEN.getName(), getRawName(),
            () -> super.sizeAsync());
    }

    @Override
    public RFuture<Integer> valueSizeAsync(K key) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HSTRLEN.getName(), getRawName(),
            String.valueOf(key), () -> super.valueSizeAsync(key));
    }

    @Override
    protected RFuture<Boolean> containsKeyOperationAsync(String name, Object key) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HEXISTS.getName(), getRawName(),
            String.valueOf(key), () -> super.containsKeyOperationAsync(name, key));
    }

    @Override
    public RFuture<Boolean> containsValueAsync(Object value) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HVALS.getName(), getRawName(),
            String.valueOf(value), () -> super.containsValueAsync(value));
    }

    @Override
    public RFuture<Set<K>> randomKeysAsync(int count) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HRANDFIELD_KEYS.getName(), getRawName(),
            () -> super.randomKeysAsync(count));
    }

    @Override
    public RFuture<Map<K, V>> randomEntriesAsync(int count) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HRANDFIELD.getName(), getRawName(),
            () -> super.randomEntriesAsync(count));
    }

    @Override
    public RFuture<Map<K, V>> getAllOperationAsync(Set<K> keys) {
        return RedissonWrapperCommon.delegateCall(redisUri, "HMGET", getRawName(), RedisKeyUtil.generate(keys),
            () -> super.getAllOperationAsync(keys));
    }

    @Override
    protected RFuture<Void> putAllOperationAsync(Map<? extends K, ? extends V> map) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HMSET.getName(), getRawName(),
            RedisKeyUtil.generate(map), () -> super.putAllOperationAsync(map));
    }

    @Override
    public RFuture<Set<K>> readAllKeySetAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HKEYS.getName(), getRawName(),
            () -> super.readAllKeySetAsync());
    }

    @Override
    public RFuture<Collection<V>> readAllValuesAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HVALS.getName(), getRawName(),
            () -> super.readAllValuesAsync());
    }

    @Override
    public RFuture<Set<Entry<K, V>>> readAllEntrySetAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate(RedisCommands.HGETALL.getName(), "entry"), getRawName(),
            () -> super.readAllEntrySetAsync());
    }

    @Override
    public RFuture<Map<K, V>> readAllMapAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HGETALL.getName(), getRawName(),
            () -> super.readAllMapAsync());
    }

    @Override
    protected RFuture<V> putIfExistsOperationAsync(K key, V value) {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate("hget", "hset", "putIfExistsOperation"), getRawName(), String.valueOf(key),
            () -> super.putIfExistsOperationAsync(key, value));
    }

    @Override
    protected RFuture<V> putIfAbsentOperationAsync(K key, V value) {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate("hsetnx", "hget", "putIfAbsentOperation"), getRawName(), String.valueOf(key),
            () -> super.putIfAbsentOperationAsync(key, value));
    }

    @Override
    protected RFuture<Boolean> fastPutIfAbsentOperationAsync(K key, V value) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HSETNX.getName(), getRawName(),
            String.valueOf(key), () -> super.fastPutIfAbsentOperationAsync(key, value));
    }

    @Override
    protected RFuture<Boolean> fastPutIfExistsOperationAsync(K key, V value) {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate("hget", "hset", "fastPutIfExistsOperation"), getRawName(), String.valueOf(key),
            () -> super.fastPutIfExistsOperationAsync(key, value));
    }

    @Override
    protected RFuture<Boolean> removeOperationAsync(Object key, Object value) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("hget", "hset", "replaceOperation2"),
            getRawName(), String.valueOf(key), () -> super.removeOperationAsync(key, value));
    }

    @Override
    protected RFuture<Boolean> replaceOperationAsync(K key, V oldValue, V newValue) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("hget", "hset", "replaceOperation3"),
            getRawName(), String.valueOf(key), () -> super.replaceOperationAsync(key, oldValue, newValue));
    }

    @Override
    protected RFuture<V> replaceOperationAsync(K key, V value) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("hget", "hset", "replaceOperation"),
            getRawName(), String.valueOf(key), () -> super.replaceOperationAsync(key, value));
    }

    @Override
    protected RFuture<Boolean> fastReplaceOperationAsync(K key, V value) {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate("hexists", "hset", "fastReplaceOperation"), getRawName(), String.valueOf(key),
            () -> super.fastReplaceOperationAsync(key, value));
    }

    @Override
    public RFuture<V> getOperationAsync(K key) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HGET.getName(), getRawName(),
            String.valueOf(key), () -> super.getOperationAsync(key));
    }

    @Override
    protected RFuture<V> putOperationAsync(K key, V value) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("hget", "hset", "putOperation"),
            getRawName(), String.valueOf(key), () -> super.putOperationAsync(key, value));
    }

    @Override
    protected RFuture<V> removeOperationAsync(K key) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HDEL.getName(), getRawName(),
            String.valueOf(key), () -> super.removeOperationAsync(key));
    }

    @Override
    protected RFuture<Boolean> fastPutOperationAsync(K key, V value) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HSET.getName(), getRawName(),
            String.valueOf(key), () -> super.fastPutOperationAsync(key, value));
    }

    @Override
    protected RFuture<List<Long>> fastRemoveOperationBatchAsync(K... keys) {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate(RedisCommands.HDEL.getName(), "batch"), getRawName(), RedisKeyUtil.generate(keys),
            () -> super.fastRemoveOperationBatchAsync(keys));
    }

    @Override
    protected RFuture<Long> fastRemoveOperationAsync(K... keys) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.HDEL.getName(), getRawName(),
            RedisKeyUtil.generate(keys), () -> super.fastRemoveOperationAsync(keys));
    }

    @Override
    protected RFuture<V> addAndGetOperationAsync(K key, Number value) {
        return RedissonWrapperCommon.delegateCall(redisUri, "HINCRBYFLOAT", getRawName(), String.valueOf(key),
            () -> super.addAndGetOperationAsync(key, value));
    }
}
