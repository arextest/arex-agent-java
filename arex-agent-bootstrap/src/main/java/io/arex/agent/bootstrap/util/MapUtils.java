package io.arex.agent.bootstrap.util;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {
    /**
     * The largest power of two that can be represented as an {@code int}.
     *
     * @since 10.0
     */
    public static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

    private MapUtils() {
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static <K, V> Map<K, V> newHashMapWithExpectedSize(int expectedSize) {
        return new HashMap<>(capacity(expectedSize));
    }

    static int capacity(int expectedSize) {
        if (expectedSize < 3) {
            return expectedSize + 1;
        }
        if (expectedSize < MAX_POWER_OF_TWO) {
            // This is the calculation used in JDK8 to resize when a putAll
            // happens; it seems to be the most conservative calculation we
            // can make.  0.75 is the default load factor.
            return (int) (expectedSize / 0.75F + 1.0F);
        }
        // any large value
        return Integer.MAX_VALUE;
    }

    public static <K> boolean getBoolean(final Map<? super K, ?> map, final K key) {
        return getBoolean(map, key, false);
    }

    public static <K> boolean getBoolean(final Map<? super K, ?> map, final K key, boolean defaultValue) {
        if (isEmpty(map) || map.get(key) == null) {
            return defaultValue;
        }
        final Object answer = map.get(key);
        if (answer instanceof Boolean) {
            return (Boolean) answer;
        }
        if (answer instanceof String) {
            return Boolean.parseBoolean((String) answer);
        }
        if (answer instanceof Number) {
            final Number num = (Number) answer;
            return num.intValue() != 0;
        }
        return defaultValue;
    }

    public static <K> String getString(final Map<? super K, ?> map, final K key, final String defaultValue) {
        String answer = getString(map, key);
        if (answer == null) {
            answer = defaultValue;
        }
        return answer;
    }

    public static <K> String getString(final Map<? super K, ?> map, final K key) {
        if (map != null) {
            final Object answer = map.get(key);
            if (answer != null) {
                return answer.toString();
            }
        }
        return null;
    }
}
