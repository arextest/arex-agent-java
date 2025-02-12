package io.arex.inst.runtime.config;

import io.arex.inst.runtime.model.CompareConfigurationEntity;
import io.arex.inst.runtime.model.DynamicClassEntity;

import java.util.*;

public class ConfigBuilder {
    private final Map<String, String> properties;
    private boolean enableDebug = false;
    private final String serviceName;
    private List<DynamicClassEntity> dynamicClassList;
    private Set<String> excludeServiceOperations;
    private int dubboStreamReplayThreshold;
    private int recordRate;
    private CompareConfigurationEntity compareConfigurationEntity;

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

    public ConfigBuilder dynamicClassList(List<DynamicClassEntity> dynamicClassList) {
        this.dynamicClassList = dynamicClassList;
        return this;
    }

    public ConfigBuilder excludeServiceOperations(Set<String> excludeServiceOperations) {
        this.excludeServiceOperations = excludeServiceOperations;
        return this;
    }

    public ConfigBuilder compareConfiguration(CompareConfigurationEntity compareConfigurationEntity) {
        this.compareConfigurationEntity = compareConfigurationEntity;
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

    public ConfigBuilder dubboStreamReplayThreshold(int dubboStreamReplayThreshold) {
        this.dubboStreamReplayThreshold = dubboStreamReplayThreshold;
        return this;
    }

    public ConfigBuilder recordRate(int recordRate) {
        this.recordRate = recordRate;
        return this;
    }

    public void build() {
        Config.update(enableDebug, serviceName, dubboStreamReplayThreshold, recordRate,
                ConfigExtendEntity.of(dynamicClassList, Collections.unmodifiableMap(new HashMap<>(properties)),
                        excludeServiceOperations, compareConfigurationEntity));
    }
}
