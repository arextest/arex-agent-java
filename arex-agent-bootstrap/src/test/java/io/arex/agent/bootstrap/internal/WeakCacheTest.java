package io.arex.agent.bootstrap.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.ref.ReferenceQueue;
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

    /**
     * Explicit remove: the ForkJoinTask exec/run exit uses it to hand the snapshot back right
     * away instead of waiting for a GC cycle. Also pins the identity semantics -- remove must
     * only drop the entry for that same object, never one that merely compares equal.
     */
    @Test
    void testRemove() {
        WeakCache<Object, String> cache = new WeakCache<>();
        Object key = new Object();
        cache.put(key, "value");
        assertTrue(cache.contains(key));

        assertEquals("value", cache.remove(key));
        assertFalse(cache.contains(key));
        assertNull(cache.remove(key));
    }

    @Test
    void testRemoveIsIdentityBasedNotEquals() {
        WeakCache<Object, String> cache = new WeakCache<>();
        String first = new StringBuilder("same").toString();
        String second = new StringBuilder("same").toString();
        cache.put(first, "first-value");
        cache.put(second, "second-value");

        assertEquals("first-value", cache.remove(first));

        assertFalse(cache.contains(first));
        assertTrue(cache.contains(second));
        assertEquals("second-value", cache.get(second));
    }

    @Test
    void testWeakReferenceKeyEqualsReturnsFalse() {
        WeakCache.WeakReferenceKey<String> key = new WeakCache.WeakReferenceKey<>("test", new ReferenceQueue<>());
        assertFalse(key.equals(null));
        assertFalse(key.equals("test"));
    }
}
