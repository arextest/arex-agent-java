package io.arex.inst.redis.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @since 2024/1/12
 */
class RedisKeyUtilTest {

    @Test
    void generateWithIterableKeys() {
        String result = RedisKeyUtil.generate(Arrays.asList("key1", "key2", "key3"));
        assertEquals("key1;key2;key3", result);
    }

    @Test
    void generateWithMapKeys() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        String result = RedisKeyUtil.generate(map);
        assertTrue(result.contains("key1"));
        assertTrue(result.contains("key2"));
    }

    @Test
    void generateWithVarargsKeys() {
        String result = RedisKeyUtil.generate("key1", "key2", "key3");
        assertEquals("key1;key2;key3", result);
    }

    @Test
    void generateWithEmptyVarargsKeys() {
        String result = RedisKeyUtil.generate();
        assertEquals("", result);
    }

    @Test
    void generateWithSingleVarargsKey() {
        String result = RedisKeyUtil.generate("key1");
        assertEquals("key1", result);
    }

    @Test
    void generateWithByteValue() {
        String result = RedisKeyUtil.generate(new byte[]{'k', 'e', 'y'});
        assertEquals("key", result);
    }

    @Test
    void generateWithCharValue() {
        String result = RedisKeyUtil.generate(new char[]{'k', 'e', 'y'});
        assertEquals("key", result);
    }
}
