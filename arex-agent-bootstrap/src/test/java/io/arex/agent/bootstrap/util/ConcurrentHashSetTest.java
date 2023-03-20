package io.arex.agent.bootstrap.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConcurrentHashSetTest {

    static ConcurrentHashSet<String> concurrentHashSet = null;

    @BeforeAll
    static void setUp() {
        concurrentHashSet = new ConcurrentHashSet<>();
        concurrentHashSet = new ConcurrentHashSet<>(8);
    }

    @AfterAll
    static void tearDown() {
        concurrentHashSet = null;
    }

    @Test
    void test() throws InterruptedException {
        // test add
        for (int i = 0; i < 5; i++) {
            String key = "key" + i;
            Executors.newCachedThreadPool().execute(() -> {
                concurrentHashSet.add(key);
            });
        }

        TimeUnit.SECONDS.sleep(1);

        // test size
        assertEquals(5, concurrentHashSet.size());

        // test contains
        for (int i = 0; i < 5; i++) {
            String key = "key" + i;
            assertTrue(concurrentHashSet.contains(key));
        }

        assertFalse(concurrentHashSet.contains("key6"));

        // test remove
        assertTrue(concurrentHashSet.remove("key1"));
        assertFalse(concurrentHashSet.remove("key6"));

        // test iterator
        assertEquals("key2", concurrentHashSet.iterator().next());

        // test isEmpty
        assertFalse(concurrentHashSet.isEmpty());
        // test clear
        concurrentHashSet.clear();
        // test isEmpty
        assertTrue(concurrentHashSet.isEmpty());
    }

    @Test
    void testEquals() {
        ConcurrentHashSet<String> set1 = new ConcurrentHashSet<>();
        set1.add("x1");
        set1.add("x2");

        ConcurrentHashSet<String> set2 = new ConcurrentHashSet<>();

        assertEquals(set1, set1);

        assertNotEquals(set1, new HashSet<>());

        assertNotEquals(set1, set2);

        set2.add("x1");
        set2.add("x2");
        assertEquals(set1, set2);
    }

    @Test
    void testHashCode() {
        ConcurrentHashSet<String> set1 = new ConcurrentHashSet<>();

        ConcurrentHashSet<String> set2 = new ConcurrentHashSet<>();

        assertEquals(set1.hashCode(), set2.hashCode());
    }
}