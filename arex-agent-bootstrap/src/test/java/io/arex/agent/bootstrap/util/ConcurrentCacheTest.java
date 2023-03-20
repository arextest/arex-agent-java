package io.arex.agent.bootstrap.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConcurrentCacheTest {
    ConcurrentCache<String, String> cache = null;

    @BeforeEach
    void setUp() {
        cache = new ConcurrentCache<>(3);
    }

    @AfterEach
    void tearDown() {
        cache = null;
    }

    @Test
    void test() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            // new string, avoid string constant pool strong reference
            String key = new String("key" + i);
            String value = "value" + i;
            Executors.newCachedThreadPool().execute(()-> {
                String result = cache.computeIfAbsent(key, key1 -> value);
                System.out.printf("cache computed key: %s, value: %s%n", key, result);
            });
            cache.computeIfAbsent(key, key1 -> value);
        }

        TimeUnit.SECONDS.sleep(1);

        for (int i = 0; i < 5; i++) {
            String key = new String("key" + i);
            String value = cache.get(key);
            System.out.printf("cache get1 key: %s, value: %s%n", key, value);
            assertNotNull(value);
        }

        System.gc();

        try {
            Field longterm = cache.getClass().getDeclaredField("longterm");
            longterm.setAccessible(true);
            Map longtermMap = (Map) longterm.get(cache);
            System.out.printf("longterm: %s%n", longtermMap);

            Field eden = cache.getClass().getDeclaredField("eden");
            eden.setAccessible(true);
            Map edenMap = (Map) eden.get(cache);
            System.out.printf("eden: %s%n", edenMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}