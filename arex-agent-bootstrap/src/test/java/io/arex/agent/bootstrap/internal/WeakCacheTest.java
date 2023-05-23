package io.arex.agent.bootstrap.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WeakCacheTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testNullKey() {
        Cache.CAPTURED_CACHE.put(null, Thread.currentThread());
        Thread actual = (Thread) Cache.CAPTURED_CACHE.get(null);
        assertEquals(actual, Thread.currentThread());
        assertTrue(Cache.CAPTURED_CACHE.contains(null));

        System.gc();

        assertTrue(Cache.CAPTURED_CACHE.contains(null));

        Cache.CAPTURED_CACHE.clear();
        assertFalse(Cache.CAPTURED_CACHE.contains(null));
    }

    @Test
    void testNullValue() {
        assertDoesNotThrow(() -> {
            Cache.CAPTURED_CACHE.put(Thread.currentThread(), null);
            Cache.CAPTURED_CACHE.clear();
        });
    }

    @Test
    void testNormalKeyValue() throws InterruptedException {
        ForkJoinTask forkJoinTask = ForkJoinTask.adapt(() -> {});
        Cache.CAPTURED_CACHE.put(forkJoinTask, Boolean.TRUE);
        Boolean actual = (Boolean) Cache.CAPTURED_CACHE.get(forkJoinTask);
        assertTrue(actual);
        assertTrue(Cache.CAPTURED_CACHE.contains(forkJoinTask));
        forkJoinTask = null;

        System.gc();

        TimeUnit.MILLISECONDS.sleep(100);
        // check -> remove after gc
        assertFalse(Cache.CAPTURED_CACHE.contains(null));
    }
}
