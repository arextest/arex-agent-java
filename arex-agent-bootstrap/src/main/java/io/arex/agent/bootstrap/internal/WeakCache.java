package io.arex.agent.bootstrap.internal;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;

public class WeakCache<K, V> extends ReferenceQueue<K> implements Cache<K, V> {
    final ConcurrentMap<WeakReferenceKey<K>, V> target;

    final CleanUpTask<V> cleanUpTask;

    public WeakCache() {
        this(new ConcurrentHashMap<>(), null);
    }

    public WeakCache(CleanUpTask<V> cleanUpTask) {
        this(new ConcurrentHashMap<>(), cleanUpTask);
    }

    public WeakCache(ConcurrentMap<WeakReferenceKey<K>, V> target, CleanUpTask<V> cleanUpTask) {
        this.target = target;
        this.cleanUpTask = cleanUpTask;
    }

    public V get(K key) {
        check();
        return target.get(new WeakReferenceKey<>(key, this));
    }

    public boolean contains(K key) {
        check();
        return target.containsKey(new WeakReferenceKey<>(key, this));
    }

    public void put(K key, V value) {
        check();
        if (value == null) {
            return;
        }
        target.put(new WeakReferenceKey<>(key, this), value);
    }

    public void clear() {
        target.clear();
    }

    public boolean containsKey(K key) {
        check();
        return target.containsKey(new WeakReferenceKey<>(key));
    }

    void check() {
        Reference<?> reference;
        while ((reference = poll()) != null) {
            final V value = target.remove(reference);
            if (cleanUpTask != null && value != null) {
                cleanUpTask.cleanUp(value);
            }
        }
    }

    static final class WeakReferenceKey<K> extends WeakReference<K> {
        private final int hashCode;

        WeakReferenceKey(K key) {
            this(key, null);
        }

        WeakReferenceKey(K key, ReferenceQueue<? super K> queue) {
            super(key, queue);
            hashCode = (key == null) ? 0 : System.identityHashCode(key);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof WeakCache.WeakReferenceKey<?>) {
                return ((WeakReferenceKey<?>) other).get() == get();
            }

            return other != null && other.equals(this);
        }
    }

    public interface CleanUpTask<T> {
        /**
         * @param object object to cleanup
         */
        void cleanUp(T object);
    }
}

