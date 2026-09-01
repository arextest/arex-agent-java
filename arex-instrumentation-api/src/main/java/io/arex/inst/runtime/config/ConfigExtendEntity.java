package io.arex.inst.runtime.config;

import io.arex.inst.runtime.model.CompareConfigurationEntity;
import io.arex.inst.runtime.model.DynamicClassEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigExtendEntity {
    private final List<DynamicClassEntity> dynamicClassList;
    private final Map<String, String> properties;
    private final Set<String> excludeServiceOperations;
    private final CompareConfigurationEntity compareConfigurationEntity;

    private ConfigExtendEntity(List<DynamicClassEntity> dynamicClassList,
                               Map<String, String> properties, Set<String> excludeServiceOperations,
                               CompareConfigurationEntity compareConfigurationEntity) {
        this.dynamicClassList = dynamicClassList;
        this.properties = properties;
        this.excludeServiceOperations = excludeServiceOperations;
        this.compareConfigurationEntity = compareConfigurationEntity;
    }

    public static ConfigExtendEntity of(List<DynamicClassEntity> dynamicClassList,
                                        Map<String, String> properties, Set<String> excludeServiceOperations,
                                        CompareConfigurationEntity compareConfigurationEntity) {
        return new ConfigExtendEntity(dynamicClassList, properties, excludeServiceOperations,
                compareConfigurationEntity);
    }

    public List<DynamicClassEntity> getDynamicClassList() {
        return dynamicClassList;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Set<String> getExcludeServiceOperations() {
        return excludeServiceOperations;
    }

    public CompareConfigurationEntity getCompareConfigurationEntity() {
        return compareConfigurationEntity;
    }
}
