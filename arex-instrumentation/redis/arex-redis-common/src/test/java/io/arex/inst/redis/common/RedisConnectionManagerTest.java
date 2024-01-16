package io.arex.inst.redis.common;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.units.qual.K;
import org.junit.jupiter.api.Test;

class RedisConnectionManagerTest {

    @Test
    void test() {
        int connectionHash = "RedisConnectionManager-test".hashCode();
        String expectedResult = "redis://127.0.0.1:6379";
        RedisConnectionManager.add(connectionHash, expectedResult);
        String actualResult = RedisConnectionManager.getRedisUri(connectionHash);
        assertNotNull(actualResult);
    }
}
