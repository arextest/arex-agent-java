package io.arex.inst.runtime.config;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.ConcurrentHashSet;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.model.DynamicClassEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.arex.agent.bootstrap.constants.ConfigConstants.STORAGE_MODE;
import static io.arex.agent.bootstrap.constants.ConfigConstants.STORAGE_SERVICE_MODE;

public class Config {

    private static Config INSTANCE = null;

    static void update(boolean enableDebug, String serviceName, List<DynamicClassEntity> dynamicClassList,
        Map<String, String> properties, Set<String> excludeServiceOperations,
        int dubboStreamReplayThreshold, int recordRate) {
        INSTANCE = new Config(enableDebug, serviceName, dynamicClassList, properties,
            excludeServiceOperations,
            dubboStreamReplayThreshold, recordRate);
    }

    public static Config get() {
        return INSTANCE;
    }

    private final boolean enableDebug;
    private final String serviceName;
    private final List<DynamicClassEntity> dynamicClassList;
    private Map<String, DynamicClassEntity> dynamicClassSignatureMap;
    private String[] dynamicAbstractClassList;
    private final Map<String, String> properties;
    private final Set<String> excludeServiceOperations;
    private final int dubboStreamReplayThreshold;
    private final int recordRate;
    private final String recordVersion;
    private final Set<String> includeServiceOperations;
    private final String[] coveragePackages;

    Config(boolean enableDebug, String serviceName, List<DynamicClassEntity> dynamicClassList,
        Map<String, String> properties,
        Set<String> excludeServiceOperations, int dubboStreamReplayThreshold, int recordRate) {
        this.enableDebug = enableDebug;
        this.serviceName = serviceName;
        this.dynamicClassList = dynamicClassList;
        this.properties = properties;
        this.excludeServiceOperations = buildExcludeServiceOperations(excludeServiceOperations);
        this.dubboStreamReplayThreshold = dubboStreamReplayThreshold;
        this.recordRate = recordRate;
        this.recordVersion = properties.get("arex.agent.version");
        this.includeServiceOperations = StringUtil.splitToSet(properties.get("includeServiceOperations"), ',');
        this.coveragePackages = StringUtil.split(properties.get(ConfigConstants.COVERAGE_PACKAGES), ',');
        buildDynamicClassInfo();
    }

    private Set<String> buildExcludeServiceOperations(Set<String> excludeServiceOperations) {
        if (excludeServiceOperations == null) {
            return Collections.emptySet();
        }
        Set<String> excludeServiceOperationSet = new ConcurrentHashSet<>();
        excludeServiceOperationSet.addAll(excludeServiceOperations);
        return excludeServiceOperationSet;
    }

    private void buildDynamicClassInfo() {
        if (dynamicClassList == null) {
            this.dynamicClassSignatureMap = Collections.emptyMap();
            this.dynamicAbstractClassList = StringUtil.EMPTY_STRING_ARRAY;
            return;
        }

        Map<String, DynamicClassEntity> map = new HashMap<>(dynamicClassList.size());
        ArrayList<String> list = new ArrayList<>(dynamicClassList.size());
        for (DynamicClassEntity entity : dynamicClassList) {
            map.putIfAbsent(entity.getSignature(), entity);
            if (entity.isAbstractClass()) {
                list.add(entity.removedAbstractClassPrefix());
            }
        }
        this.dynamicClassSignatureMap = map;
        this.dynamicAbstractClassList = list.toArray(StringUtil.EMPTY_STRING_ARRAY);
    }

    public String[] getCoveragePackages() {
        return coveragePackages;
    }
    public String getRecordVersion() {
        return recordVersion;
    }

    public DynamicClassEntity getDynamicEntity(String methodSignature) {
        return dynamicClassSignatureMap.get(methodSignature);
    }

    public Map<String, DynamicClassEntity> getDynamicClassSignatureMap() {
        return dynamicClassSignatureMap;
    }

    public String[] getDynamicAbstractClassList() {
        return dynamicAbstractClassList;
    }

    public boolean isEnableDebug() {
        return this.enableDebug;
    }

    public List<DynamicClassEntity> getDynamicClassList() {
        return this.dynamicClassList;
    }

    public Set<String> excludeServiceOperations() {
        return this.excludeServiceOperations;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getString(String name) {
        return getString(name, null);
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

    public int getRecordRate() {
        return recordRate;
    }

    public Set<String> getIncludeServiceOperations() {
        return includeServiceOperations;
    }

    public boolean isLocalStorage() {
        return STORAGE_MODE.equalsIgnoreCase(getString(STORAGE_SERVICE_MODE));
    }

    /**
     * Conditions for determining invalid recording configuration:<br/>
     * 1. rate <= 0 <br/>
     * 2. not in working time <br/>
     * 3. exceed rate limit <br/>
     * 4. local IP match target IP <br/>
     * 5. record disabled by config
     *
     * @return true: invalid, false: valid
     */
    public boolean invalidRecord(String path) {
        if (isLocalStorage()) {
            return false;
        }
        if (getRecordRate() <= 0) {
            return true;
        }
        if (!getBoolean(ConfigConstants.DURING_WORK, false)) {
            return true;
        }
        if (!getBoolean(ConfigConstants.AGENT_ENABLED, false)) {
            return true;
        }
        if (getBoolean(ConfigConstants.DISABLE_RECORD, false)) {
            return true;
        }

        return !RecordLimiter.acquire(path);
    }
}
