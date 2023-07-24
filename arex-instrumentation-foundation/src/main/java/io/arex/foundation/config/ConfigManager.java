package io.arex.foundation.config;

import com.google.common.annotations.VisibleForTesting;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.model.ConfigQueryResponse.DynamicClassConfiguration;
import io.arex.foundation.model.ConfigQueryResponse.ResponseBody;
import io.arex.foundation.model.ConfigQueryResponse.ServiceCollectConfig;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.foundation.util.NetUtils;
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

import static io.arex.foundation.config.ConfigConstants.*;

public class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    public static final ConfigManager INSTANCE = new ConfigManager();
    public static final AtomicBoolean FIRST_TRANSFORM = new AtomicBoolean(false);
    private static final int DEFAULT_RECORDING_RATE = 1;
    private boolean enableDebug;
    private boolean enableReportStatus;
    private String agentVersion;
    private String serviceName;
    private String storageServiceHost;
    private String configPath;

    private String storageServiceMode;
    private int recordRate;
    private int dynamicResultSizeLimit;
    private final List<DynamicClassEntity> dynamicClassList = new ArrayList<>();
    /**
     * use only replay
     */
    private boolean startTimeMachine;
    private EnumSet<DayOfWeek> allowDayOfWeeks;
    private LocalTime allowTimeOfDayFrom;
    private LocalTime allowTimeOfDayTo;
    private List<String> disabledModules;
    private List<String> retransformModules;
    private Set<String> excludeServiceOperations;
    private String targetAddress;
    private int dubboStreamReplayThreshold;
    private boolean disableReplay;
    private List<ConfigListener> listeners = new ArrayList<>();
    private Map<String, String> extendField;

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

    public boolean isEnableReportStatus() {
        return enableReportStatus;
    }

    public void setEnableReportStatus(String enableReportStatus) {
        if (StringUtil.isEmpty(enableReportStatus)) {
            return;
        }

        this.enableReportStatus = Boolean.parseBoolean(enableReportStatus);
        System.setProperty(ENABLE_REPORT_STATUS, enableReportStatus);
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

    public void setTimeMachine(String timeMachine) {
        if (StringUtil.isEmpty(timeMachine)) {
            return;
        }
        this.startTimeMachine = Boolean.parseBoolean(timeMachine);
        System.setProperty(TIME_MACHINE, timeMachine);
    }

    public List<DynamicClassEntity> getDynamicClassList() {
        return dynamicClassList;
    }

    public void setDynamicClassList(List<DynamicClassConfiguration> newDynamicCOnfigList) {
        if (newDynamicCOnfigList == null) {
            return;
        }
        // reset previously configured dynamic classes
        if (newDynamicCOnfigList.isEmpty()) {
            for (DynamicClassEntity item : dynamicClassList) {
                item.setStatus(DynamicClassStatusEnum.RESET);
            }
            return;
        }

        List<DynamicClassEntity> newDynamicClassList = new ArrayList<>(newDynamicCOnfigList.size());
        for (DynamicClassConfiguration config : newDynamicCOnfigList) {
            DynamicClassEntity newItem = new DynamicClassEntity(config.getFullClassName(), config.getMethodName(),
                config.getParameterTypes(), config.getKeyFormula());
            newItem.setStatus(DynamicClassStatusEnum.RETRANSFORM);
            newDynamicClassList.add(newItem);
        }

        // if old dynamic class list is empty, add all new dynamic class list
        if (dynamicClassList.isEmpty()) {
            dynamicClassList.addAll(newDynamicClassList);
            return;
        }

        // check if dynamic classes changed
        if (dynamicClassList.size() == newDynamicClassList.size() && dynamicClassList.equals(newDynamicClassList)) {
            return;
        }

        Set<String> resetClassSet = new HashSet<>();
        List<DynamicClassEntity> unchangedList = new ArrayList<>();
        for (DynamicClassEntity entity : dynamicClassList) {
            if (newDynamicClassList.contains(entity)) {
                entity.setStatus(DynamicClassStatusEnum.UNCHANGED);
                unchangedList.add(entity);
            } else {
                entity.setStatus(DynamicClassStatusEnum.RESET);
                resetClassSet.add(entity.getClazzName());
            }
        }
        // reset unchanged dynamic classes status to retransform
        for (DynamicClassEntity entity : unchangedList) {
            if (resetClassSet.contains(entity.getClazzName())) {
                entity.setStatus(DynamicClassStatusEnum.RETRANSFORM);
            }
        }
        List<DynamicClassEntity> retransformList = new ArrayList<>();
        for (DynamicClassEntity entity : newDynamicClassList) {
            if (!dynamicClassList.contains(entity)) {
                retransformList.add(entity);
            }
        }
        dynamicClassList.addAll(retransformList);
    }

    @VisibleForTesting
    void init() {
        agentVersion = System.getProperty(AGENT_VERSION);
        setEnableDebug(System.getProperty(ENABLE_DEBUG));
        setEnableReportStatus(System.getProperty(ENABLE_REPORT_STATUS, Boolean.TRUE.toString()));
        setServiceName(StringUtil.strip(System.getProperty(SERVICE_NAME)));
        setStorageServiceHost(StringUtil.strip(System.getProperty(STORAGE_SERVICE_HOST)));
        configPath = StringUtil.strip(System.getProperty(CONFIG_PATH));
        setRecordRate(DEFAULT_RECORDING_RATE);

        setStorageServiceMode(System.getProperty(STORAGE_SERVICE_MODE));
        setDynamicResultSizeLimit(System.getProperty(DYNAMIC_RESULT_SIZE_LIMIT, "1000"));
        setTimeMachine(System.getProperty(TIME_MACHINE));
        setAllowDayOfWeeks(Integer.parseInt(System.getProperty(ALLOW_DAY_WEEKS, "127")));
        setAllowTimeOfDayFrom(System.getProperty(ALLOW_TIME_FROM, "00:01"));
        setAllowTimeOfDayTo(System.getProperty(ALLOW_TIME_TO, "23:59"));
        setDisabledModules(System.getProperty(DISABLE_MODULE));
        setRetransformModules(System.getProperty(RETRANSFORM_MODULE));
        setExcludeServiceOperations(System.getProperty(EXCLUDE_SERVICE_OPERATION));
        setDubboStreamReplayThreshold(System.getProperty(DUBBO_STREAM_REPLAY_THRESHOLD, "100"));
        setDisableReplay(System.getProperty(DISABLE_REPLAY));
    }

    @VisibleForTesting
    void readConfigFromFile(String configPath) {
        if (StringUtil.isEmpty(configPath)) {
            return;
        }

        Map<String, String> configMap = parseConfigFile(configPath);
        if (configMap.size() == 0) {
            return;
        }

        setEnableDebug(configMap.get(ENABLE_DEBUG));
        setEnableReportStatus(System.getProperty(ENABLE_REPORT_STATUS));
        setServiceName(configMap.get(SERVICE_NAME));
        setStorageServiceHost(configMap.get(STORAGE_SERVICE_HOST));
        setDynamicResultSizeLimit(configMap.get(DYNAMIC_RESULT_SIZE_LIMIT));
        setTimeMachine(configMap.get(TIME_MACHINE));
        setStorageServiceMode(configMap.get(STORAGE_SERVICE_MODE));
        setDisabledModules(configMap.get(DISABLE_MODULE));
        setRetransformModules(configMap.get(RETRANSFORM_MODULE));
        setExcludeServiceOperations(configMap.get(EXCLUDE_SERVICE_OPERATION));
        setDisableReplay(configMap.get(DISABLE_REPLAY));
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
        if (!agentMap.isEmpty()) {
            setStorageServiceMode(agentMap.get(STORAGE_SERVICE_MODE));
            setEnableDebug(agentMap.get(ENABLE_DEBUG));
            updateRuntimeConfig();
        }
    }

    public void updateConfigFromService(ResponseBody serviceConfig) {
        ServiceCollectConfig config = serviceConfig.getServiceCollectConfiguration();
        setRecordRate(config.getSampleRate());
        setTimeMachine(String.valueOf(config.isTimeMock()));
        setAllowDayOfWeeks(config.getAllowDayOfWeeks());
        setAllowTimeOfDayFrom(config.getAllowTimeOfDayFrom());
        setAllowTimeOfDayTo(config.getAllowTimeOfDayTo());
        setDynamicClassList(serviceConfig.getDynamicClassConfigurationList());
        setExcludeServiceOperations(config.getExcludeServiceOperationSet());
        setTargetAddress(serviceConfig.getTargetAddress());
        setExtendField(serviceConfig.getExtendField());

        updateRuntimeConfig();
    }

    private void updateRuntimeConfig() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put(DYNAMIC_RESULT_SIZE_LIMIT, String.valueOf(getDynamicResultSizeLimit()));
        configMap.put(TIME_MACHINE, String.valueOf(startTimeMachine()));
        configMap.put(DISABLE_REPLAY, String.valueOf(disableReplay()));
        configMap.put(DURING_WORK, Boolean.toString(nextWorkTime() <= 0));
        configMap.put(AGENT_VERSION, agentVersion);
        configMap.put(IP_VALIDATE, Boolean.toString(checkTargetAddress()));
        Map<String, String> extendFieldMap = getExtendField();
        if (extendFieldMap != null && !extendFieldMap.isEmpty()) {
            configMap.putAll(extendFieldMap);
        }

        ConfigBuilder.create(getServiceName())
            .enableDebug(isEnableDebug())
            .addProperties(configMap)
            .dynamicClassList(getDynamicClassList().stream()
                .filter(item -> DynamicClassStatusEnum.RESET != item.getStatus()).collect(Collectors.toList()))
            .excludeServiceOperations(getExcludeServiceOperations())
            .dubboStreamReplayThreshold(getDubboStreamReplayThreshold())
            .recordRate(getRecordRate())
            .build();
        publish(Config.get());
    }

    private void publish(Config config) {
        for (ConfigListener listener : listeners) {
            if (listener.validate(config)) {
                listener.load(config);
            }
        }
    }

    public boolean valid() {
        if (isLocalStorage()) {
            return true;
        }
        return checkTargetAddress() && inWorkingTime();
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

    public boolean startTimeMachine() {
        return startTimeMachine;
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
            return;
        }
        this.excludeServiceOperations = excludeServiceOperationSet;
    }

    public Set<String> getExcludeServiceOperations() {
        return excludeServiceOperations;
    }

    public void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress;
    }

    private boolean checkTargetAddress() {
        String localHost = NetUtils.getIpAddress();
        // Compatible containers can't get IPAddress
        if (StringUtil.isEmpty(localHost)) {
            return true;
        }

        return localHost.equals(targetAddress);
    }

    public void setDubboStreamReplayThreshold(String dubboStreamReplayThreshold) {
        this.dubboStreamReplayThreshold = Integer.parseInt(dubboStreamReplayThreshold);
    }

    public int getDubboStreamReplayThreshold() {
        return dubboStreamReplayThreshold;
    }

    public void setDisableReplay(String disableReplay) {
        if (StringUtil.isEmpty(disableReplay)) {
            return;
        }

        this.disableReplay = Boolean.parseBoolean(disableReplay);
        System.setProperty(DISABLE_REPLAY, disableReplay);
    }

    public boolean disableReplay() {
        return disableReplay;
    }

    public Map<String, String> getExtendField() {
        return extendField;
    }

    public void setExtendField(Map<String, String> extendField) {
        this.extendField = extendField;
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
            ", startTimeMachine='" + startTimeMachine + '\'' +
            ", allowDayOfWeeks='" + allowDayOfWeeks + '\'' +
            ", allowTimeOfDayFrom='" + allowTimeOfDayFrom + '\'' +
            ", allowTimeOfDayTo='" + allowTimeOfDayTo + '\'' +
            ", dynamicClassList='" + dynamicClassList + '\'' +
            '}';
    }
}
