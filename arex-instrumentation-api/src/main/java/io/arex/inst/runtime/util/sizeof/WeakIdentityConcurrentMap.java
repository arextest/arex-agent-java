package io.arex.inst.runtime.util.sizeof;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WeakIdentityConcurrentMap<K, V> {
    private final ConcurrentMap<WeakReference<K>, V> map = new ConcurrentHashMap<>();
    private final ReferenceQueue<K> queue = new ReferenceQueue<>();

    private final CleanUpTask<V> cleanUpTask;

    /**
     * Constructor
     */
    public WeakIdentityConcurrentMap() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param cleanUpTask task cleaning up references
     */
    public WeakIdentityConcurrentMap(final CleanUpTask<V> cleanUpTask) {
        this.cleanUpTask = cleanUpTask;
    }

    /**
     * Puts into the underlying
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>,
     *         if the implementation supports <tt>null</tt> values.)
     */
    public V put(K key, V value) {
        cleanUp();
        return map.put(new IdentityWeakReference<>(key, queue), value);
    }

    /**
     * Remove from the underlying
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     */
    public V remove(K key) {
        cleanUp();
        return map.remove(new IdentityWeakReference<>(key, queue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        cleanUp();
        return map.toString();
    }

    /**
     * Puts into the underlying
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with the key,
     *         if the implementation supports null values.)
     */
    public V putIfAbsent(K key, V value) {
        cleanUp();
        return map.putIfAbsent(new IdentityWeakReference<>(key, queue), value);
    }

    /**
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     */
    public V get(K key) {
        cleanUp();
        return map.get(new IdentityWeakReference<>(key));
    }

    /**
     *
     */
    public void cleanUp() {

        Reference<? extends K> reference;
        while ((reference = queue.poll()) != null) {
            final V value = map.remove(reference);
            if (cleanUpTask != null && value != null) {
                cleanUpTask.cleanUp(value);
            }
        }
    }

    /**
     * @return a set view of the keys contained in this map
     */
    public Set<K> keySet() {
        cleanUp();
        K k;
        final HashSet<K> ks = new HashSet<>();
        for (WeakReference<K> weakReference : map.keySet()) {
            k = weakReference.get();
            if (k != null) {
                ks.add(k);
            }
        }
        return ks;
    }

    public boolean containsKey(final K key) {
        cleanUp();
        return map.containsKey(new IdentityWeakReference<>(key));
    }

    /**
     * @param <T>
     */
    private static final class IdentityWeakReference<T> extends WeakReference<T> {

        private final int hashCode;

        /**
         * @param reference the referenced object
         */
        IdentityWeakReference(T reference) {
            this(reference, null);
        }

        /**
         * @param reference the references object
         * @param referenceQueue the reference queue where references are kept
         */
        IdentityWeakReference(T reference, ReferenceQueue<T> referenceQueue) {
            super(reference, referenceQueue);
            this.hashCode = (reference == null) ? 0 : System.identityHashCode(reference);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.valueOf(get());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof IdentityWeakReference<?>)) {
                return false;
            } else {
                IdentityWeakReference<?> wr = (IdentityWeakReference<?>)o;
                Object got = get();
                return (got != null && got == wr.get());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    /**
     * @param <T>
     */
    public interface CleanUpTask<T> {

        /**
         * @param object object to cleanup
         */
        void cleanUp(T object);
    }
}
