package io.arex.agent.bootstrap.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

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
}
