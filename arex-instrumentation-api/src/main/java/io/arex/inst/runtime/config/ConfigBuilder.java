package io.arex.inst.runtime.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigBuilder {
    private final Map<String, String> properties;
    private boolean enableDebug = false;
    private final String serviceName;

    public static ConfigBuilder create(String serviceName) {
        return new ConfigBuilder(serviceName);
    }

    public ConfigBuilder(String serviceName) {
        this.serviceName = serviceName;
        properties = new HashMap<>();
    }

    public ConfigBuilder enableDebug(boolean enableDebug) {
        this.enableDebug = enableDebug;
        return this;
    }

    public ConfigBuilder addProperty(String name, String value) {
        if (value != null) {
            properties.put(name, value);
        }
        return this;
    }

    // for: arex-instrumentation module
    public ConfigBuilder addProperties(Properties properties) {
        if (properties != null) {
            for (String name : properties.stringPropertyNames()) {
                addProperty(name, properties.getProperty(name));
            }
        }
        return this;
    }

    public ConfigBuilder addProperties(Map<String, String> configMap) {
        properties.putAll(configMap);
        return this;
    }

    public void build() {
        Config.update(enableDebug, serviceName, Collections.unmodifiableMap(new HashMap<>(properties)));
    }
}
