package io.arex.inst.runtime.config;

import java.util.Map;
import java.util.function.Function;

public class Config {
    private static volatile Config INSTANCE = null;

    static void update(boolean enableDebug, String serviceName, Map<String, String> properties) {
        INSTANCE = new Config(enableDebug, serviceName, properties);
    }

    public static Config get() {
        return INSTANCE;
    }

    private boolean enableDebug;
    private String serviceName;
    private Map<String, String> properties;

    Config(boolean enableDebug, String serviceName, Map<String, String> properties) {
        this.enableDebug = enableDebug;
        this.serviceName = serviceName;
        this.properties = properties;
    }

    public boolean isEnableDebug() {
        return this.enableDebug;
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
}
