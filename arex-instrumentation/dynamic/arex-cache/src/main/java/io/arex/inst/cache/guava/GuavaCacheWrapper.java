package io.arex.inst.cache.guava;

import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CompatibleWith;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.cache.common.CacheWrapperCommon;
import io.arex.inst.cache.common.CacheLoaderUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import static io.arex.inst.dynamic.common.DynamicClassExtractor.GUAVA_IMMUTABLE_MAP;

public class GuavaCacheWrapper<K, V> implements LoadingCache<K, V> {
    private final String cacheLoaderName;
    private final LoadingCache<K, V> loadingCache;

    public GuavaCacheWrapper(Object cacheLoader, LoadingCache<K, V> loadingCache) {
        this.cacheLoaderName = CacheLoaderUtil.getLocatedClass(cacheLoader);
        this.loadingCache = loadingCache;
    }

    @Override
    public V get(K key) throws ExecutionException {
        return CacheWrapperCommon.processWithException(cacheLoaderName, "get", key, StringUtil.EMPTY, k -> loadingCache.get(key));
    }

    @Override
    public V getUnchecked(K key) {
        return CacheWrapperCommon.process(cacheLoaderName, "getUnchecked", key, StringUtil.EMPTY, k -> loadingCache.getUnchecked(key));
    }

    @Override
    public ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException {
        return CacheWrapperCommon.processWithException(cacheLoaderName, "getAll", keys, GUAVA_IMMUTABLE_MAP, k -> loadingCache.getAll(keys));
    }

    @Nullable
    @Override
    public V getIfPresent(@CompatibleWith("K") Object key) {
        return CacheWrapperCommon.process(cacheLoaderName, "getIfPresent", key, StringUtil.EMPTY, k -> loadingCache.getIfPresent(key));
    }

    @Override
    public V get(K key, Callable<? extends V> loader) throws ExecutionException {
        return CacheWrapperCommon.processWithException(cacheLoaderName, "getWithCallable", key, StringUtil.EMPTY, k -> loadingCache.get(key, loader));
    }

    @Override
    public ImmutableMap<K, V> getAllPresent(Iterable<?> keys) {
        return CacheWrapperCommon.process(cacheLoaderName, "getAllPresent", keys, GUAVA_IMMUTABLE_MAP, k -> loadingCache.getAllPresent(keys));
    }

    @Override
    public void put(K key, V value) {
        loadingCache.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        loadingCache.putAll(m);
    }

    @Override
    public void invalidate(@CompatibleWith("K") Object key) {
        loadingCache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        loadingCache.invalidateAll();
    }

    @Override
    public long size() {
        return loadingCache.size();
    }

    @Override
    public CacheStats stats() {
        return loadingCache.stats();
    }

    @Override
    public void invalidateAll(Iterable<?> keys) {
        loadingCache.invalidateAll(keys);
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return loadingCache.asMap();
    }

    @Override
    public void cleanUp() {
        loadingCache.cleanUp();
    }

    @Override
    public V apply(K key) {
        return loadingCache.apply(key);
    }

    @Override
    public void refresh(K key) {
        loadingCache.refresh(key);
    }
}
