package io.arex.inst.runtime.config;

import io.arex.inst.runtime.model.DynamicClassEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class Config {
    private static Config INSTANCE = null;

    static void update(boolean enableDebug, String serviceName, List<DynamicClassEntity> entities,
                       Map<String, String> properties, Set<String> excludeServiceOperations, int dubboStreamReplayThreshold) {
        INSTANCE = new Config(enableDebug, serviceName, entities, properties, excludeServiceOperations, dubboStreamReplayThreshold);
    }

    public static Config get() {
        return INSTANCE;
    }

    private final boolean enableDebug;
    private final String serviceName;
    private final List<DynamicClassEntity> entities;
    private Map<String, String> properties;
    private Set<String> excludeServiceOperations;
    private final int dubboStreamReplayThreshold;
    Config(boolean enableDebug, String serviceName, List<DynamicClassEntity> entities, Map<String, String> properties,
           Set<String> excludeServiceOperations, int dubboStreamReplayThreshold) {
        this.enableDebug = enableDebug;
        this.serviceName = serviceName;
        this.entities = entities;
        this.properties = properties;
        this.excludeServiceOperations = excludeServiceOperations;
        this.dubboStreamReplayThreshold = dubboStreamReplayThreshold;
    }

    public boolean isEnableDebug() {
        return this.enableDebug;
    }

    public List<DynamicClassEntity> dynamicClassEntities() {
        return this.entities;
    }

    public Set<String> excludeServiceOperations() {
        return this.excludeServiceOperations;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getString(String name) {
        return getRawProperty(name, null);
    }

    public String getString(String name, String defaultValue) {
        return getRawProperty(name, defaultValue);
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        return safeGetTypedProperty(name, Boolean::parseBoolean, defaultValue);
    }

    public int getInt(String name, int defaultValue) {
        return safeGetTypedProperty(name, Integer::parseInt, defaultValue);
    }

    public long getLong(String name, long defaultValue) {
        return safeGetTypedProperty(name, Long::parseLong, defaultValue);
    }

    public double getDouble(String name, double defaultValue) {
        return safeGetTypedProperty(name, Double::parseDouble, defaultValue);
    }

    private <T> T safeGetTypedProperty(String name, Function<String, T> parser, T defaultValue) {
        try {
            T value = getTypedProperty(name, parser);
            return value == null ? defaultValue : value;
        } catch (RuntimeException t) {
            return defaultValue;
        }
    }

    private <T> T getTypedProperty(String name, Function<String, T> parser) {
        String value = getRawProperty(name, null);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return parser.apply(value);
    }

    private String getRawProperty(String name, String defaultValue) {
        return this.properties.getOrDefault(name, defaultValue);
    }

    public int getDubboStreamReplayThreshold() {
        return dubboStreamReplayThreshold;
    }
}
