package io.arex.agent.bootstrap.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentHashSet <T> extends AbstractSet<T> {
    private final ConcurrentMap<T, Boolean> delegate;

    public ConcurrentHashSet() {
        this.delegate = new ConcurrentHashMap<>();
    }

    public ConcurrentHashSet(int initialCapacity) {
        this.delegate = new ConcurrentHashMap<>(initialCapacity);
    }

    @Override
    public boolean add(T t) {
        return delegate.put(t, Boolean.TRUE) == null;
    }

    @Override
    public boolean remove(Object o) {
        return Boolean.TRUE.equals(delegate.remove(o));
    }

    @Override
    public boolean contains(Object o) {
        return delegate.containsKey(o);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.keySet().iterator();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConcurrentHashSet)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ConcurrentHashSet<?> that = (ConcurrentHashSet<?>) o;

        return Objects.equals(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + delegate.hashCode();
        return result;
    }
}
