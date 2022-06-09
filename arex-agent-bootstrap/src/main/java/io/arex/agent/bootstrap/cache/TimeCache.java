package io.arex.agent.bootstrap.cache;

import io.arex.agent.bootstrap.TraceContextManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TimeCache {
    private static final ConcurrentHashMap<String, Long> CACHE = new ConcurrentHashMap(30);

    public static final String PREFIX_MILLIS = "MILLIS-";
    public static final String PREFIX_NANO = "NANO-";

    public static Long get() {
        String traceId = TraceContextManager.get();
        Long millis = CACHE.get(PREFIX_MILLIS + traceId);
        if (millis != null) {
            long nanoTime = CACHE.get(PREFIX_NANO + traceId);
            // increase the time difference from the first replay
            return millis + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nanoTime);
        }
        return 0L;
    }

    public static void put(String prefix, Long value) {
        String traceId = TraceContextManager.get();
        if (traceId != null) {
            CACHE.put(prefix + traceId, value);
        }
    }

    public static void remove() {
        String traceId = TraceContextManager.get();
        if (traceId != null) {
            CACHE.remove(traceId);
        }
    }
}
