package io.arex.foundation.serializer.util;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.serializer.jackson.JacksonSerializerWithType;
import io.arex.inst.runtime.log.LogManager;

import java.lang.reflect.Field;

/**
 * <relocation>
 *    <pattern>com.google</pattern>
 *    <shadedPattern>shaded.com.google</shadedPattern>
 * </relocation>
 * after packaging, com.google.** will be relocated to shaded.com.google.**. String also will be relocated.
 * so in order to get the Range type at runtime, need to remove the shaded prefix
 * ex: String GUAVA_RANGE_CLASS_NAME = com.google.common.collect.Range -> shaded.com.google.common.collect.Range
 */
public class GuavaRangeManager {
    public static final String SHADED_PREFIX = "shaded.";
    public static final String GUAVA_RANGE_CLASS_NAME = StringUtil.substringAfter("com.google.common.collect.Range", SHADED_PREFIX);
    public static final String GUAVA_BELOW_ALL_CLASS_NAME = StringUtil.substringAfter("com.google.common.collect.Cut$BelowAll", SHADED_PREFIX);
    public static final String GUAVA_ABOVE_ALL_CLASS_NAME = StringUtil.substringAfter("com.google.common.collect.Cut$AboveAll", SHADED_PREFIX);

    private static Class<?> rangeClass;
    private static Field lowerBoundField;
    private static Field upperBoundField;
    private static Object belowAllInstance;
    private static Object aboveAllInstance;

    static {
        try {
            rangeClass = Class.forName(GUAVA_RANGE_CLASS_NAME);
            Class<?> belowAll = Class.forName(GUAVA_BELOW_ALL_CLASS_NAME);
            Class<?> aboveAll = Class.forName(GUAVA_ABOVE_ALL_CLASS_NAME);
            Field belowAllInstanceField = belowAll.getDeclaredField("INSTANCE");
            belowAllInstanceField.setAccessible(true);
            belowAllInstance = belowAllInstanceField.get(null);
            Field aboveAllInstanceField = aboveAll.getDeclaredField("INSTANCE");
            aboveAllInstanceField.setAccessible(true);
            aboveAllInstance = aboveAllInstanceField.get(null);
            lowerBoundField = rangeClass.getDeclaredField("lowerBound");
            upperBoundField = rangeClass.getDeclaredField("upperBound");
            lowerBoundField.setAccessible(true);
            upperBoundField.setAccessible(true);
        } catch (Exception ignored) {
            LogManager.warn("rang.init","Failed to load Range class");
        }
    }
    private GuavaRangeManager() {
    }

    public static Object restoreRange(String json) {
        try {
            Object rangeDeserializer = JacksonSerializerWithType.INSTANCE.deserialize(json, rangeClass);
            if (StringUtil.containsIgnoreCase(json, GUAVA_BELOW_ALL_CLASS_NAME)) {
                lowerBoundField.set(rangeDeserializer, belowAllInstance);
            }
            if (StringUtil.containsIgnoreCase(json, GUAVA_ABOVE_ALL_CLASS_NAME)) {
                upperBoundField.set(rangeDeserializer, aboveAllInstance);
            }
            return rangeDeserializer;
        } catch (Throwable e) {
            LogManager.warn("rang.restore", e);
            return null;
        }
    }

    /**
     * Guava_RANGE will be shaded, so use endswith.
     */
    public static boolean isGuavaRange(String typeName) {
        return GUAVA_RANGE_CLASS_NAME.endsWith(typeName);
    }
}
