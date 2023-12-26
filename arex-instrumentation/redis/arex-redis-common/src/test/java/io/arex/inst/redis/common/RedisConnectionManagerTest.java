package io.arex.inst.redis.common;

import static org.junit.jupiter.api.Assertions.*;

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
