package io.arex.inst.cache.caffeine;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.arex.inst.cache.common.CacheWrapperCommon;
import io.arex.inst.cache.common.CacheLoaderUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.arex.inst.dynamic.common.DynamicClassExtractor.COMPLETABLE_FUTURE;

public class AsyncLoadingCacheWrapper<K, V> implements AsyncLoadingCache<K, V> {
    private final String cacheLoaderName;
    private final AsyncLoadingCache<K, V> asyncLoadingCache;

    public AsyncLoadingCacheWrapper(Object cacheLoader, AsyncLoadingCache<K, V> asyncLoadingCache) {
        this.cacheLoaderName = CacheLoaderUtil.getLocatedClass(cacheLoader);
        this.asyncLoadingCache = asyncLoadingCache;
    }

    @Override
    public CompletableFuture<V> get(K key) {
        return CacheWrapperCommon.process(cacheLoaderName, "get", key, COMPLETABLE_FUTURE, k -> asyncLoadingCache.get(key));
    }

    @Override
    public CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys) {
        return CacheWrapperCommon.process(cacheLoaderName, "getAll", keys, COMPLETABLE_FUTURE, k -> asyncLoadingCache.getAll(keys));
    }

    @Override
    public @Nullable CompletableFuture<V> getIfPresent(K key) {
        return CacheWrapperCommon.process(cacheLoaderName, "getIfPresent", key, COMPLETABLE_FUTURE, k -> asyncLoadingCache.getIfPresent(key));
    }

    @Override
    public CompletableFuture<V> get(K key, Function<? super K, ? extends V> mappingFunction) {
        return CacheWrapperCommon.process(cacheLoaderName, "getWithFunction", key, COMPLETABLE_FUTURE, k -> asyncLoadingCache.get(key, mappingFunction));
    }

    @Override
    public CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys,
                                               BiFunction<? super Set<? extends K>, ? super Executor,
                                                       ? extends CompletableFuture<? extends Map<? extends K, ? extends V>>> mappingFunction) {
        return CacheWrapperCommon.process(cacheLoaderName, "getAllWithBiFunction", keys, COMPLETABLE_FUTURE, k -> asyncLoadingCache.getAll(keys, mappingFunction));
    }

    @Override
    public CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys,
                                               Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        return CacheWrapperCommon.process(cacheLoaderName, "getAllWithFunction", keys, COMPLETABLE_FUTURE, k -> asyncLoadingCache.getAll(keys, mappingFunction));
    }

    @Override
    public CompletableFuture<V> get(K key, BiFunction<? super K, ? super Executor,
            ? extends CompletableFuture<? extends V>> mappingFunction) {
        return CacheWrapperCommon.process(cacheLoaderName, "getWithBiFunction", key, COMPLETABLE_FUTURE, k -> asyncLoadingCache.get(key, mappingFunction));
    }

    @Override
    public void put(K key, CompletableFuture<? extends V> valueFuture) {
        asyncLoadingCache.put(key, valueFuture);
    }

    @Override
    public ConcurrentMap<K, CompletableFuture<V>> asMap() {
        return asyncLoadingCache.asMap();
    }

    @Override
    public LoadingCache<K, V> synchronous() {
        return asyncLoadingCache.synchronous();
    }
}
