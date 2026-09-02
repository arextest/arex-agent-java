package io.arex.agent.bootstrap.internal;

public interface Cache<K, V> {

    /**
     * Declared as the concrete {@link WeakCache} rather than {@code Cache} so the ForkJoinTask
     * exec/run exit advice can drop its entry explicitly. {@code Cache} deliberately does not
     * expose remove: {@link TrieCache} is a prefix tree and has no matching semantics for it.
     *
     * <p>Weak keys alone are not enough here. The captured snapshot is the map <em>value</em>,
     * so it stays strongly reachable from this static field: it survives every young GC and is
     * promoted to the old generation. And an entry only leaves the map when a later put/get
     * happens to drain the reference queue, so entries linger once traffic stops.
     */
    WeakCache<Object, Object> CAPTURED_CACHE = new WeakCache<>();

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
