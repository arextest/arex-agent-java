package io.arex.inst.lettuce.v5.cluster;

import io.arex.inst.redis.common.RedisConnectionManager;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;

import java.util.concurrent.CompletableFuture;

public class RedisClusterClientExtractor {

    private RedisClusterClientExtractor() {}

    public static <K, V> void addConnection(CompletableFuture<StatefulRedisClusterConnection<K, V>> connectionFuture, Iterable<RedisURI> redisURIs) {
        connectionFuture.thenAccept(connection ->
            // take first uri
            RedisConnectionManager.add(connection.hashCode(), redisURIs.iterator().next().toString()));
    }
}
