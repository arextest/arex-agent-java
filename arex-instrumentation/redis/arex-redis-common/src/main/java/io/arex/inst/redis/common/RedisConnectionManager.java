package io.arex.inst.redis.common;

import io.arex.agent.bootstrap.util.StringUtil;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class RedisConnectionManager {
    private static final Map<Integer, String> REDIS_URI_MAP = new ConcurrentHashMap<>();

    private RedisConnectionManager() {
    }

    public static void add(int connectionHash, String redisUri) {
        REDIS_URI_MAP.put(connectionHash, redisUri);
    }

    public static String getRedisUri(int connectionHash) {
        return REDIS_URI_MAP.get(connectionHash);
    }

    public static <K, V> void addClusterConnection(CompletableFuture<StatefulRedisClusterConnection<K, V>> connectionFuture, Iterable<RedisURI> redisURIs) {
        connectionFuture.thenAccept(
            connection -> RedisConnectionManager.add(connection.hashCode(), StringUtil.join(redisURIs, ",")));
    }
}
