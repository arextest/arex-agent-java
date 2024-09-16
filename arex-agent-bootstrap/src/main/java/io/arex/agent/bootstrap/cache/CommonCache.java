package io.arex.agent.bootstrap.cache;

import java.util.concurrent.ConcurrentHashMap;

public class CommonCache {
    private static final ConcurrentHashMap<String, Object> CACHE = new ConcurrentHashMap<>();

    public static Object get(String key) {
        return CACHE.get(key);
    }

    public static void put(String key, Object value) {
        CACHE.put(key, value);
    }
}
