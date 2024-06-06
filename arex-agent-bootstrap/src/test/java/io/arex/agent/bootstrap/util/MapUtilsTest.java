package io.arex.agent.bootstrap.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MapUtilsTest {

    @Test
    void isEmpty() {
        assertTrue(MapUtils.isEmpty(null));
        assertTrue(MapUtils.isEmpty(Collections.emptyMap()));
    }

    @Test
    void isNotEmpty() {
        assertFalse(MapUtils.isNotEmpty(Collections.emptyMap()));
    }

    @Test
    void newHashMapWithExpectedSize() {
        Map<String, String> actualMap = MapUtils.newHashMapWithExpectedSize(1);
        assertEquals(2, capacity(actualMap));

        actualMap = MapUtils.newHashMapWithExpectedSize(3);
        assertEquals(8, capacity(actualMap));
    }


    public static int capacity(Map<String, String> map) {
        try {
            Class<?> mapType = map.getClass();
            Method capacity = mapType.getDeclaredMethod("capacity");
            capacity.setAccessible(true);
            return (int) capacity.invoke(map);
        } catch (Exception e) {
            return 0;
        }
    }

    @ParameterizedTest
    @MethodSource("getBooleanCase")
    void getBoolean(Map<String, Object> map, String key, Predicate<Boolean> asserts) {
        asserts.test(MapUtils.getBoolean(map, key));
    }

    static Stream<Arguments> getBooleanCase() {
        Map<String, Object> map = new HashMap<>();
        map.put("booleanKey", true);
        map.put("stringKey", "true");
        map.put("numberKey", 1);
        map.put("key", String.class);

        Predicate<Boolean> asserts1 = bool -> bool;
        Predicate<Boolean> asserts2 = bool -> !bool;

        return Stream.of(
                arguments(null, null, asserts1),
                arguments(map, "booleanKey", asserts2),
                arguments(map, "stringKey", asserts2),
                arguments(map, "numberKey", asserts2),
                arguments(map, "key", asserts1)
        );
    }

    @Test
    void getString() {
        // null map
        assertNull(MapUtils.getString(null, "key"));
        Map<String, String> map = new HashMap<>();
        // empty map
        assertNull(MapUtils.getString(map, "key"));
        // key exist return value
        map.put("key", "value");
        assertEquals("value", MapUtils.getString(map, "key", "default"));
        // key not exist return default value
        assertEquals("default", MapUtils.getString(map, "key1", "default"));
    }

    @Test
    void putIfValueNotNull() {
        Map<String, Object> map = new HashMap<>();
        MapUtils.putIfValueNotNull(map, "key", "value");
        assertEquals("value", map.get("key"));

        MapUtils.putIfValueNotNull(map, "key", null);
        assertNotNull(map.get("key"));
    }
}
