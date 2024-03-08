package io.arex.inst.cache.guava;

import java.util.concurrent.ExecutionException;

public interface FunctionWithException<K, V> {
    V apply(K key) throws ExecutionException;
}
