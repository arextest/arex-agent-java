package io.arex.inst.cache.caffeine;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.cache.common.CacheWrapperCommon;
import io.arex.inst.cache.common.CacheLoaderUtil;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class LoadingCacheWrapper<K, V> implements LoadingCache<K, V> {
    private final String cacheLoaderName;
    private final LoadingCache<K, V> loadingCache;

    public LoadingCacheWrapper(Object cacheLoader, LoadingCache<K, V> loadingCache) {
        this.cacheLoaderName = CacheLoaderUtil.getLocatedClass(cacheLoader);
        this.loadingCache = loadingCache;
    }

    @Override
    public V get(K key) {
        return CacheWrapperCommon.process(cacheLoaderName, "get", key, StringUtil.EMPTY, k -> loadingCache.get(key));
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        return CacheWrapperCommon.process(cacheLoaderName, "getAll", keys, StringUtil.EMPTY, k -> loadingCache.getAll(keys));
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                            Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        return CacheWrapperCommon.process(cacheLoaderName, "getAllWithFunction", keys, StringUtil.EMPTY, k -> loadingCache.getAll(keys, mappingFunction));
    }

    @Nullable
    @Override
    public V getIfPresent(K key) {
        return CacheWrapperCommon.process(cacheLoaderName, "getIfPresent", key, StringUtil.EMPTY, k -> loadingCache.getIfPresent(key));
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> mappingFunction) {
        return CacheWrapperCommon.process(cacheLoaderName, "getWithFunction", key, StringUtil.EMPTY, k -> loadingCache.get(key, mappingFunction));
    }

    @Override
    public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
        return CacheWrapperCommon.process(cacheLoaderName, "getAllPresent", keys, StringUtil.EMPTY, k -> loadingCache.getAllPresent(keys));
    }

    @Override
    public void put(K key, V value) {
        loadingCache.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        loadingCache.putAll(map);
    }

    @Override
    public void invalidate(K key) {
        loadingCache.invalidate(key);
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        loadingCache.invalidateAll(keys);
    }

    @Override
    public void invalidateAll() {
        loadingCache.invalidateAll();
    }

    @Override
    public @NonNegative long estimatedSize() {
        return loadingCache.estimatedSize();
    }

    @Override
    public CacheStats stats() {
        return loadingCache.stats();
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
    public Policy<K, V> policy() {
        return loadingCache.policy();
    }

    @Override
    public CompletableFuture<V> refresh(K key) {
        return loadingCache.refresh(key);
    }

    @Override
    public CompletableFuture<Map<K, V>> refreshAll(Iterable<? extends K> keys) {
        return loadingCache.refreshAll(keys);
    }
}
