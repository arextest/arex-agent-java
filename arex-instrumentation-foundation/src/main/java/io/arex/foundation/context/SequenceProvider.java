package io.arex.foundation.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SequenceProvider {
    static final int OFFSET_BASIS = 0x811C9DC5;
    static final int FNV_PRIME = 16777619;

    private final ConcurrentHashMap<Integer, AtomicInteger> sequence = new ConcurrentHashMap<>(10);

    public int get(String target) {
        int key = generateKey(target);
        return sequence.computeIfAbsent(key, k -> new AtomicInteger(0)).addAndGet(1);
    }

    private int generateKey(String value) {
        int key = OFFSET_BASIS;
        for (int i = 0; i < value.length(); i++) {
            key ^= value.charAt(i);
            key *= FNV_PRIME;
        }
        return key;
    }
}
