package io.arex.inst.time;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.cache.TimeCache;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @since 2024/1/12
 */
class TimeMachineInterceptorTest {
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(TraceContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void onEnter() {
        // traceId is not null
        Mockito.when(TraceContextManager.get()).thenReturn("test");
        assertEquals(0, TimeCache.get());
        TimeCache.put(1L);
        assertNotEquals(0, TimeMachineInterceptor.onEnter());
    }

    @Test
    void onExit() {
        long result = 456L;
        TimeMachineInterceptor.onExit(123L, result);
        assertEquals(456L, result);
    }
}
