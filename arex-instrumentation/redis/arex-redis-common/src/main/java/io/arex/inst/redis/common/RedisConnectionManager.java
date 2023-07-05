package io.arex.inst.redis.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RedisConnectionManager {
    private static final Map<Integer, String> REDIS_URI_MAP = new ConcurrentHashMap<>();

    public static void add(int connectionHash, String redisUri) {
        REDIS_URI_MAP.put(connectionHash, redisUri);
    }


    public static String getRedisUri(int connectionHash) {
        return REDIS_URI_MAP.get(connectionHash);
    }

    private RedisConnectionManager() {
    }
}
