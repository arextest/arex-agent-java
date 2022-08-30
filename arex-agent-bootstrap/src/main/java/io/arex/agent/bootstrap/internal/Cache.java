package io.arex.agent.bootstrap.internal;

public interface Cache<K, V> {

    Cache<Object, Object> CAPTURED_CACHE = weakMap();

    static <K, V> Cache<K, V> weakMap() {
        return new WeakCache<>();
    }

    static <V> Cache<String, V> trieCache() {
        return new TrieCache<>();
    }

    static <V> Cache<String, V> trieCacheWithInit(String init) {
        return new TrieCache<>(init);
    }

    V get(K key);
    void put(K key, V value);

    void clear();

    boolean contains(K key);
}
