package io.arex.foundation.config;

import com.google.common.annotations.VisibleForTesting;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.model.ConfigQueryResponse.DynamicClassConfiguration;
import io.arex.foundation.model.ConfigQueryResponse.ResponseBody;
import io.arex.foundation.model.ConfigQueryResponse.ServiceCollectConfig;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.config.listener.ConfigListener;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.model.DynamicClassStatusEnum;
import io.arex.agent.bootstrap.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static io.arex.agent.bootstrap.constants.ConfigConstants.*;

public class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    public static final ConfigManager INSTANCE = new ConfigManager();
    public static final AtomicBoolean FIRST_TRANSFORM = new AtomicBoolean(false);
    private static final int DEFAULT_RECORDING_RATE = 1;
    private boolean enableDebug;
    private String agentVersion;
    private String serviceName;
    private String storageServiceHost;
    private String configServiceHost;
    private String configPath;

    private String storageServiceMode;
    private int recordRate;
    private int dynamicResultSizeLimit;
    private List<DynamicClassEntity> dynamicClassList = new ArrayList<>();
    private Set<String> resetClassSet = new HashSet<>();
    private EnumSet<DayOfWeek> allowDayOfWeeks;
    private LocalTime allowTimeOfDayFrom;
    private LocalTime allowTimeOfDayTo;
    private List<String> ignoredTypePrefixes;
    private List<String> ignoredClassLoaders;
    private List<String> disabledModules;
    private List<String> retransformModules;
    private Set<String> excludeServiceOperations;
    private boolean agentEnabled;
    private String message;
    private int dubboStreamReplayThreshold;
    private List<ConfigListener> listeners = new ArrayList<>();
    private Map<String, String> extendField;
    private int bufferSize;

    private ConfigManager() {
        init();
        initConfigListener();
        readConfigFromFile(configPath);
        updateRuntimeConfig();
    }

    private void initConfigListener() {
        listeners = ServiceLoader.load(ConfigListener.class);
    }

    public boolean isEnableDebug() {
        return enableDebug;
    }

    public void setEnableDebug(String enableDebug) {
        if (StringUtil.isEmpty(enableDebug)) {
            return;
        }

        this.enableDebug = Boolean.parseBoolean(enableDebug);
        System.setProperty(ENABLE_DEBUG, enableDebug);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        if (StringUtil.isEmpty(serviceName)) {
            return;
        }

        this.serviceName = serviceName;
        System.setProperty(SERVICE_NAME, serviceName);
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public String getStorageServiceHost() {
        return storageServiceHost;
    }

    public void setStorageServiceHost(String storageServiceHost) {
        if (StringUtil.isEmpty(storageServiceHost)) {
            return;
        }
        this.storageServiceHost = storageServiceHost;
        System.setProperty(STORAGE_SERVICE_HOST, storageServiceHost);
    }

    public String getConfigServiceHost() {
        if (StringUtil.isNotEmpty(configServiceHost)) {
            return configServiceHost;
        }
        return storageServiceHost;
    }

    public void setConfigServiceHost(String configServiceHost) {
        if (StringUtil.isEmpty(configServiceHost)) {
            return;
        }
        this.configServiceHost = configServiceHost;
        System.setProperty(CONFIG_SERVICE_HOST, configServiceHost);
    }

    public void setRecordRate(int recordRate) {
        if (recordRate < 0) {
            return;
        }
        this.recordRate = recordRate;
        System.setProperty(RECORD_RATE, String.valueOf(recordRate));
    }

    public int getRecordRate() {
        return recordRate;
    }

    public Set<String> getResetClassSet() {
        return resetClassSet;
    }

    public List<DynamicClassEntity> getDynamicClassList() {
        return dynamicClassList;
    }

    /**
     * DynamicClass retransform rules:
     * 1. addEntity: all entity of the same class of the added entity class need to be set to retransform
     * 2. resetEntity: resetEntityClassSet only contains the class not in newDynamicClassList
     * ex: oldDynamicClassList: ClassA methodA, ClassB methodB, ClassC methodC
     *     newDynamicClassList: ClassA methodA, ClassA methodA2, ClassB methodB2
     *     addEntityClassSet: ClassA, ClassB
     *     resetEntityClassSet: ClassC
     *     retransform: ClassA: methodA, methodA2, ClassB methodB2
     */
    public void setDynamicClassList(List<DynamicClassConfiguration> newDynamicConfigList) {
        if (newDynamicConfigList == null) {
            return;
        }
        List<DynamicClassEntity> newDynamicClassList = new ArrayList<>(newDynamicConfigList.size());
        // keyFormula: java.lang.System.currentTimeMillis,java.util.UUID.randomUUID -> 2 dynamic classes will be created
        for (DynamicClassConfiguration config : newDynamicConfigList) {
            final String[] split = StringUtil.split(config.getKeyFormula(), ',');

            if (ArrayUtils.isEmpty(split)) {
                newDynamicClassList.add(createDynamicClass(config, config.getKeyFormula()));
                continue;
            }

            for (String keyFormula : split) {
                newDynamicClassList.add(createDynamicClass(config, keyFormula));
            }
        }

        // check if dynamic classes changed
        if (dynamicClassList.size() == newDynamicClassList.size() && dynamicClassList.equals(newDynamicClassList)) {
            setRetransformClassCollection(Collections.emptySet(), newDynamicClassList);
            return;
        }

        final Map<String, List<DynamicClassEntity>> newDynamicMap = newDynamicClassList.stream()
                .collect(Collectors.groupingBy(DynamicClassEntity::getClazzName));
        Set<String> changedClassSet = getDiffClassSet(dynamicClassList, newDynamicClassList);
        for (String clazzName : changedClassSet) {
            final List<DynamicClassEntity> needRetransformEntities = newDynamicMap.get(clazzName);
            if (CollectionUtil.isEmpty(needRetransformEntities)) {
                continue;
            }
            for (DynamicClassEntity entity : needRetransformEntities) {
                entity.setStatus(DynamicClassStatusEnum.RETRANSFORM);
            }
        }

        // resetEntityClassSet only contains the class not in newDynamicClassList
        changedClassSet.removeAll(newDynamicMap.keySet());
        setRetransformClassCollection(changedClassSet, newDynamicClassList);
    }

    private Set<String> getDiffClassSet(List<DynamicClassEntity> dynamicClassList, List<DynamicClassEntity> newDynamicClassList) {
        Set<String> changedClassSet = new HashSet<>();
        // if old dynamic class list is empty, add all new dynamic class list
        if (CollectionUtil.isEmpty(dynamicClassList)) {
            for (DynamicClassEntity entity : newDynamicClassList) {
                changedClassSet.add(entity.getClazzName());
            }
            return changedClassSet;
        }

        if (CollectionUtil.isEmpty(newDynamicClassList)) {
            for (DynamicClassEntity entity : dynamicClassList) {
                changedClassSet.add(entity.getClazzName());
            }
            return changedClassSet;
        }

        // addedEntityClassSet = newDynamicClassList - oldDynamicClassList
        for (DynamicClassEntity entity : newDynamicClassList) {
            if (!dynamicClassList.contains(entity)) {
                changedClassSet.add(entity.getClazzName());
            }
        }
        // removeEntityClassSet = oldDynamicClassList - newDynamicClassList
        for (DynamicClassEntity entity : dynamicClassList) {
            if (!newDynamicClassList.contains(entity)) {
                changedClassSet.add(entity.getClazzName());
            }
        }
        return changedClassSet;
    }

    private void setRetransformClassCollection(Set<String> resetClassSet, List<DynamicClassEntity> newDynamicClassList) {
        this.resetClassSet = resetClassSet;
        this.dynamicClassList = newDynamicClassList;
    }

    private DynamicClassEntity createDynamicClass(DynamicClassConfiguration config, String keyFormula) {
        DynamicClassEntity newItem = new DynamicClassEntity(config.getFullClassName(), config.getMethodName(),
                config.getParameterTypes(), keyFormula);
        newItem.setStatus(DynamicClassStatusEnum.UNCHANGED);
        return newItem;
    }

    @VisibleForTesting
    void init() {
        agentVersion = System.getProperty(AGENT_VERSION);
        setEnableDebug(System.getProperty(ENABLE_DEBUG));
        setServiceName(StringUtil.strip(System.getProperty(SERVICE_NAME)));
        setStorageServiceHost(StringUtil.strip(System.getProperty(STORAGE_SERVICE_HOST)));
        setConfigServiceHost(StringUtil.strip(System.getProperty(CONFIG_SERVICE_HOST)));
        configPath = StringUtil.strip(System.getProperty(CONFIG_PATH));
        setRecordRate(DEFAULT_RECORDING_RATE);

        setStorageServiceMode(System.getProperty(STORAGE_SERVICE_MODE));
        setDynamicResultSizeLimit(System.getProperty(DYNAMIC_RESULT_SIZE_LIMIT, "1000"));
        setAllowDayOfWeeks(Integer.parseInt(System.getProperty(ALLOW_DAY_WEEKS, "127")));
        setAllowTimeOfDayFrom(System.getProperty(ALLOW_TIME_FROM, "00:01"));
        setAllowTimeOfDayTo(System.getProperty(ALLOW_TIME_TO, "23:59"));
        setIgnoredTypePrefixes(System.getProperty(IGNORED_TYPES));
        setIgnoredClassLoaders(System.getProperty(IGNORED_CLASS_LOADERS));
        setDisabledModules(System.getProperty(DISABLE_MODULE));
        setRetransformModules(System.getProperty(RETRANSFORM_MODULE, "dynamic-class"));
        setExcludeServiceOperations(System.getProperty(EXCLUDE_SERVICE_OPERATION));
        setDubboStreamReplayThreshold(System.getProperty(DUBBO_STREAM_REPLAY_THRESHOLD, "100"));
        setBufferSize(System.getProperty(BUFFER_SIZE, "1024"));
    }

    @VisibleForTesting
    void readConfigFromFile(String configPath) {
        if (StringUtil.isEmpty(configPath)) {
            return;
        }

        Map<String, String> configMap = parseConfigFile(configPath);
        if (configMap.isEmpty()) {
            return;
        }

        setEnableDebug(configMap.get(ENABLE_DEBUG));
        setServiceName(configMap.get(SERVICE_NAME));
        setStorageServiceHost(configMap.get(STORAGE_SERVICE_HOST));
        setConfigServiceHost(configMap.get(CONFIG_SERVICE_HOST));
        setDynamicResultSizeLimit(configMap.get(DYNAMIC_RESULT_SIZE_LIMIT));
        setStorageServiceMode(configMap.get(STORAGE_SERVICE_MODE));
        setDisabledModules(configMap.get(DISABLE_MODULE));
        setRetransformModules(configMap.getOrDefault(RETRANSFORM_MODULE, "dynamic-class"));
        setExcludeServiceOperations(configMap.get(EXCLUDE_SERVICE_OPERATION));
        System.setProperty(DISABLE_REPLAY, StringUtil.defaultString(configMap.get(DISABLE_REPLAY)));
        System.setProperty(DISABLE_RECORD, StringUtil.defaultString(configMap.get(DISABLE_RECORD)));
        setBufferSize(configMap.get(BUFFER_SIZE));
    }

    private static Map<String, String> parseConfigFile(String configPath) {
        Map<String, String> configMap = new HashMap<>();
        try (Stream<String> configStream = Files.lines(Paths.get(configPath))) {
            configStream.forEach(item -> {
                int separatorIndex = item.indexOf('=');
                if (separatorIndex < 0) {
                    return;
                }
                String key = item.substring(0, separatorIndex);
                String value = StringUtil.strip(item.substring(separatorIndex + 1));
                configMap.put(key, value);
            });
        } catch (IOException e) {
            LOGGER.warn("Parse config file failed", e);
        }

        return configMap;
    }

    public void parseAgentConfig(String args) {
        Map<String, String> agentMap = StringUtil.asMap(args);
        if (agentMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : agentMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            switch (key) {
                case ENABLE_DEBUG:
                    setEnableDebug(value);
                    break;
                case STORAGE_SERVICE_MODE:
                    setStorageServiceMode(value);
                    break;
                case STORAGE_SERVICE_HOST:
                case DISABLE_MODULE:
                    break;
                default:
                    System.setProperty(key, value);
                    break;
            }
        }
        updateRuntimeConfig();
    }

    public void updateConfigFromService(ResponseBody serviceConfig) {
        ServiceCollectConfig config = serviceConfig.getServiceCollectConfiguration();
        setRecordRate(config.getSampleRate());
        setAllowDayOfWeeks(config.getAllowDayOfWeeks());
        setAllowTimeOfDayFrom(config.getAllowTimeOfDayFrom());
        setAllowTimeOfDayTo(config.getAllowTimeOfDayTo());
        setDynamicClassList(serviceConfig.getDynamicClassConfigurationList());
        setExcludeServiceOperations(config.getExcludeServiceOperationSet());
        setAgentEnabled(serviceConfig.isAgentEnabled());
        setExtendField(serviceConfig.getExtendField());
        setMessage(serviceConfig.getMessage());

        updateRuntimeConfig();
    }

    private void updateRuntimeConfig() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put(DYNAMIC_RESULT_SIZE_LIMIT, String.valueOf(getDynamicResultSizeLimit()));
        configMap.put(DISABLE_REPLAY, System.getProperty(DISABLE_REPLAY));
        configMap.put(DISABLE_RECORD, System.getProperty(DISABLE_RECORD));
        configMap.put(COVERAGE_PACKAGES, System.getProperty(COVERAGE_PACKAGES));
        configMap.put(DURING_WORK, Boolean.toString(inWorkingTime()));
        configMap.put(AGENT_VERSION, agentVersion);
        configMap.put(AGENT_ENABLED, Boolean.toString(agentEnabled));
        configMap.put(STORAGE_SERVICE_MODE, storageServiceMode);
        configMap.put(DISABLE_MODULE, System.getProperty(DISABLE_MODULE));
        Map<String, String> extendFieldMap = getExtendField();
        if (MapUtils.isNotEmpty(extendFieldMap)) {
            configMap.putAll(extendFieldMap);
            setEnableDebug(extendFieldMap.get(ENABLE_DEBUG));
            appendCoveragePackages(extendFieldMap.get(COVERAGE_PACKAGES));
        }

        ConfigBuilder.create(getServiceName())
            .enableDebug(isEnableDebug())
            .addProperties(configMap)
            .dynamicClassList(getDynamicClassList())
            .excludeServiceOperations(getExcludeServiceOperations())
            .dubboStreamReplayThreshold(getDubboStreamReplayThreshold())
            .recordRate(getRecordRate())
            .build();
        publish(Config.get());
    }

    private void appendCoveragePackages(String collectCoveragePackages) {
        if (StringUtil.isEmpty(collectCoveragePackages)) {
            return;
        }
        System.setProperty(COVERAGE_PACKAGES, collectCoveragePackages);
    }

    private void publish(Config config) {
        for (ConfigListener listener : listeners) {
            listener.load(config);
        }
    }

    public void setConfigInvalid() {
        setRecordRate(0);
        setAllowDayOfWeeks(0);
        updateRuntimeConfig();
    }

    public boolean isLocalStorage() {
        return STORAGE_MODE.equalsIgnoreCase(storageServiceMode);
    }

    public void setStorageServiceMode(String storageServiceMode) {
        if (StringUtil.isEmpty(storageServiceMode)) {
            return;
        }
        this.storageServiceMode = storageServiceMode;
    }

    public int getDynamicResultSizeLimit() {
        return dynamicResultSizeLimit;
    }

    public void setDynamicResultSizeLimit(String dynamicResultSizeLimit) {
        if (StringUtil.isEmpty(dynamicResultSizeLimit)) {
            return;
        }
        this.dynamicResultSizeLimit = Integer.parseInt(dynamicResultSizeLimit);
        System.setProperty(DYNAMIC_RESULT_SIZE_LIMIT, dynamicResultSizeLimit);
    }

    public EnumSet<DayOfWeek> getAllowDayOfWeeks() {
        return allowDayOfWeeks;
    }

    public void setAllowDayOfWeeks(int allowDayOfWeeks) {
        if (allowDayOfWeeks <= 0) {
            this.allowDayOfWeeks = EnumSet.noneOf(DayOfWeek.class);
            return;
        }
        // binary 1111111
        if (allowDayOfWeeks == 127) {
            this.allowDayOfWeeks = EnumSet.allOf(DayOfWeek.class);
            return;
        }
        EnumSet<DayOfWeek> dayOfWeeks = EnumSet.noneOf(DayOfWeek.class);
        String recordCycle = Integer.toBinaryString(allowDayOfWeeks);
        int index = 0;
        for (int length = recordCycle.length() - 1; length >= 0; length--) {
            index++;
            if (recordCycle.charAt(length) == '1') {
                dayOfWeeks.add(DayOfWeek.of(index));
            }
        }
        this.allowDayOfWeeks = dayOfWeeks;
        System.setProperty(ALLOW_DAY_WEEKS, String.valueOf(allowDayOfWeeks));
    }

    public LocalTime getAllowTimeOfDayFrom() {
        return allowTimeOfDayFrom;
    }

    public void setAllowTimeOfDayFrom(String allowTimeOfDayFrom) {
        if (StringUtil.isEmpty(allowTimeOfDayFrom)) {
            return;
        }
        this.allowTimeOfDayFrom = LocalTime.parse(allowTimeOfDayFrom,
            DateTimeFormatter.ofPattern("HH:mm"));
        System.setProperty(ALLOW_TIME_FROM, allowTimeOfDayFrom);
    }

    public LocalTime getAllowTimeOfDayTo() {
        return allowTimeOfDayTo;
    }

    public void setAllowTimeOfDayTo(String allowTimeOfDayTo) {
        if (StringUtil.isEmpty(allowTimeOfDayTo)) {
            return;
        }
        this.allowTimeOfDayTo = LocalTime.parse(allowTimeOfDayTo,
            DateTimeFormatter.ofPattern("HH:mm"));
        System.setProperty(ALLOW_TIME_TO, allowTimeOfDayTo);
    }

    public boolean inWorkingTime() {
        return nextWorkTime() <= 0L;
    }

    private long nextWorkTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        LocalTime beginTime = dateTime.toLocalTime();
        int diffDays = 0;
        if (beginTime.isAfter(allowTimeOfDayFrom) && beginTime.isAfter(allowTimeOfDayTo)) {
            diffDays++;
            dateTime = dateTime.plusDays(1);
        }
        while (!allowDayOfWeeks.contains(dateTime.getDayOfWeek()) && diffDays < 7) {
            diffDays++;
            dateTime = dateTime.plusDays(1);
        }
        LocalDateTime nextTime = LocalDateTime.of(dateTime.toLocalDate(), allowTimeOfDayFrom);
        return Duration.between(LocalDateTime.now(), nextTime).toMillis();
    }

    public List<String> getIgnoredTypePrefixes() {
        return ignoredTypePrefixes;
    }

    public void setIgnoredTypePrefixes(String ignoredTypes) {
        if (this.ignoredTypePrefixes == null && StringUtil.isEmpty(ignoredTypes)) {
            this.ignoredTypePrefixes = Collections.emptyList();
        } else {
            this.ignoredTypePrefixes = Arrays.asList(StringUtil.split(ignoredTypes, ','));
        }
    }


    public List<String> getIgnoredClassLoaders() {
        return ignoredClassLoaders;
    }

    public void setIgnoredClassLoaders(String ignoredClassLoaders) {
        if (StringUtil.isEmpty(ignoredClassLoaders)) {
            this.ignoredClassLoaders = Collections.emptyList();
        } else {
            this.ignoredClassLoaders = Arrays.asList(StringUtil.split(ignoredClassLoaders, ','));
        }
    }

    public List<String> getDisabledModules() {
        return disabledModules;
    }

    public void setDisabledModules(String disabledModules) {
        if (StringUtil.isEmpty(disabledModules)) {
            if (this.disabledModules == null) {
                this.disabledModules = Collections.emptyList();
            }
            return;
        }

        this.disabledModules = Arrays.asList(StringUtil.split(disabledModules, ','));
    }


    public List<String> getRetransformModules() {
        return retransformModules;
    }

    public void setRetransformModules(String retransformModules) {
        if (StringUtil.isEmpty(retransformModules)) {
            if (this.retransformModules == null) {
                this.retransformModules = Collections.emptyList();
            }
            return;
        }

        this.retransformModules = Arrays.asList(StringUtil.split(retransformModules, ','));
    }

    public void setExcludeServiceOperations(String excludeServiceOperations) {
        if (StringUtil.isEmpty(excludeServiceOperations)) {
            if (this.excludeServiceOperations == null) {
                this.excludeServiceOperations = Collections.emptySet();
            }
            return;
        }

        this.excludeServiceOperations = new HashSet<>(
            Arrays.asList(StringUtil.split(excludeServiceOperations, ',')));
    }

    public void setExcludeServiceOperations(Set<String> excludeServiceOperationSet) {
        if (CollectionUtil.isEmpty(excludeServiceOperationSet)) {
            this.excludeServiceOperations = Collections.emptySet();
            return;
        }
        this.excludeServiceOperations = excludeServiceOperationSet;
    }

    public Set<String> getExcludeServiceOperations() {
        return excludeServiceOperations;
    }

    public boolean isAgentEnabled() {
        return agentEnabled;
    }

    public void setAgentEnabled(boolean agentEnabled) {
        this.agentEnabled = agentEnabled;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDubboStreamReplayThreshold(String dubboStreamReplayThreshold) {
        this.dubboStreamReplayThreshold = Integer.parseInt(dubboStreamReplayThreshold);
    }

    public int getDubboStreamReplayThreshold() {
        return dubboStreamReplayThreshold;
    }

    public Map<String, String> getExtendField() {
        return extendField;
    }

    public void setExtendField(Map<String, String> extendField) {
        this.extendField = extendField;
    }

    public int getBufferSize() {
        if (extendField != null && extendField.containsKey(BUFFER_SIZE)) {
            return Integer.parseInt(extendField.get(BUFFER_SIZE));
        }
        return bufferSize;
    }

    public void setBufferSize(String bufferSize) {
        if (StringUtil.isEmpty(bufferSize)) {
            return;
        }
        this.bufferSize = Integer.parseInt(bufferSize);
        System.setProperty(BUFFER_SIZE, bufferSize);
    }

    @Override
    public String toString() {
        return "ConfigManager{" +
            "enableDebug=" + enableDebug +
            ", agentVersion='" + agentVersion + '\'' +
            ", serviceName='" + serviceName + '\'' +
            ", storageServiceHost='" + storageServiceHost + '\'' +
            ", configPath='" + configPath + '\'' +
            ", storageServiceMode='" + storageServiceMode + '\'' +
            ", recordRate='" + recordRate + '\'' +
            ", allowDayOfWeeks='" + allowDayOfWeeks + '\'' +
            ", allowTimeOfDayFrom='" + allowTimeOfDayFrom + '\'' +
            ", allowTimeOfDayTo='" + allowTimeOfDayTo + '\'' +
            ", dynamicClassList='" + dynamicClassList + '\'' +
            '}';
    }
}
