package io.arex.inst.lettuce.v5;

import io.lettuce.core.RedisURI;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LettuceHelper
 */
public class LettuceHelper {
    private static final Map<Integer, String> REDIS_URI_MAP = new ConcurrentHashMap<>();

    public static void putToUriMap(int connectionHash, RedisURI redisURI) {
        REDIS_URI_MAP.put(connectionHash, redisURI.toString());
    }


    public static String getRedisUri(int connectionHash) {
        return REDIS_URI_MAP.get(connectionHash);
    }
}
