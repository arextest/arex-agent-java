package io.arex.agent.bootstrap.cache;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.internal.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TimeCache {
    private static final ConcurrentHashMap<String, Pair<Long, Long>> CACHE = new ConcurrentHashMap<>(30);

    public static long get() {
        String traceId = TraceContextManager.get();
        if (traceId == null) {
            return 0L;
        }
        Pair<Long, Long> time = CACHE.get(traceId);
        return time == null ? 0L :
                time.getFirst() + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time.getSecond());
    }

    public static void put(long value) {
        String traceId = TraceContextManager.get();
        if (traceId != null) {
            CACHE.put(traceId, Pair.of(value, System.nanoTime()));
        }
    }
    public static void remove() {
        String traceId = TraceContextManager.get();
        if (traceId != null) {
            CACHE.remove(traceId);
        }
    }
}
