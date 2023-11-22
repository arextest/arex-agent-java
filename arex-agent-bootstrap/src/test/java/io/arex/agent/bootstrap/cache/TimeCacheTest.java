package io.arex.agent.bootstrap.cache;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.internal.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
class TimeCacheTest {
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(TraceContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void getAndPutAndRemove() {
        // traceId is null
        Mockito.when(TraceContextManager.get()).thenReturn(null);
        assertEquals(0, TimeCache.get());
        TimeCache.put(1L);
        assertEquals(0, TimeCache.get());

        // traceId is not null
        Mockito.when(TraceContextManager.get()).thenReturn("test");
        assertEquals(0, TimeCache.get());
        TimeCache.put(1L);
        assertNotEquals(0, TimeCache.get());

        // remove
        TimeCache.remove();
        assertEquals(0, TimeCache.get());
    }
}
