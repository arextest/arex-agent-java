package io.arex.agent.bootstrap.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DubboStreamCacheTest {
    @Test
    void put() {
        DubboStreamCache.put("mock-stream-id", "mock-trace-id", null);
        DubboStreamCache.put("mock-stream-id", "mock-trace-id", null);
        DubboStreamCache.clear();
        assertNotNull(DubboStreamCache.get("mock-stream-id"));
    }

    @Test
    void getTraceId() {
        assertNull(DubboStreamCache.getTraceId("mock-stream-id"));
    }

    @Test
    void remove() {
        DubboStreamCache.remove("mock-stream-id");
        assertNull(DubboStreamCache.getTraceId("mock-stream-id"));
    }
}