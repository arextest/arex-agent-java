package io.arex.agent.bootstrap.internal;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;

class WeakCache<K, V> extends ReferenceQueue<K> implements Cache<K, V> {
    final ConcurrentMap<WeakReferenceKey<K>, V> target;

    public WeakCache() {
        this(new ConcurrentHashMap<>());
    }

    public WeakCache(ConcurrentMap<WeakReferenceKey<K>, V> target) {
        this.target = target;
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

    void check() {
        Reference<?> reference;
        while ((reference = poll()) != null) {
            target.remove(reference);
        }
    }

    static final class WeakReferenceKey<K> extends WeakReference<K> {
        private final int hashCode;

        WeakReferenceKey(K key, ReferenceQueue<? super K> queue) {
            super(key, queue);

            hashCode = System.identityHashCode(key);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof WeakCache.WeakReferenceKey<?>) {
                return ((WeakReferenceKey<?>) other).get() == get();
            } else {
                return other.equals(this);
            }
        }
    }
}

