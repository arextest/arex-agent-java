package io.arex.foundation.config;

import com.google.common.annotations.VisibleForTesting;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.services.ConfigService;
import io.arex.foundation.services.TimerService;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.foundation.util.NetUtils;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.config.ConfigListener;
import io.arex.inst.runtime.model.DynamicClassEntity;
import org.apache.commons.lang3.StringUtils;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.arex.foundation.config.ConfigConstants.*;

// todo: use file
public class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    public static final ConfigManager INSTANCE = new ConfigManager();

    private boolean enableDebug;
    private String agentVersion;
    private String serviceName;
    private String storageServiceHost;
    private String configPath;

    private String storageServiceMode;
    private int recordRate;
    private int dynamicResultSizeLimit;
    private List<DynamicClassEntity> dynamicClassList;
    /**
     * use only replay
     */
    private boolean startTimeMachine;
    private EnumSet<DayOfWeek> allowDayOfWeeks;
    private LocalTime allowTimeOfDayFrom;
    private LocalTime allowTimeOfDayTo;
    private List<String> disabledInstrumentationModules;
    private Set<String> excludeServiceOperations;
    private String targetAddress;
    private int dubboStreamReplayThreshold;
    private boolean disableReplay;
    private static List<ConfigListener> listeners = new ArrayList<ConfigListener>();
    private Map<String, String> extendField;

    private ConfigManager() {
        init();
        readConfigFromFile(configPath);
        updateInstrumentationConfig();
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

    public void setRecordRate(String recordRate) {
        if (StringUtil.isEmpty(recordRate)) {
            return;
        }
        this.recordRate = Integer.parseInt(recordRate);
        System.setProperty(RECORD_RATE, recordRate);
    }

    public void setTimeMachine(String timeMachine) {
        if (StringUtil.isEmpty(timeMachine)) {
            return;
        }
        this.startTimeMachine = Boolean.parseBoolean(timeMachine);
        System.setProperty(TIME_MACHINE, timeMachine);
    }

    public void setDynamicClassList(
        List<ConfigService.DynamicClassConfiguration> dynamicClassConfigList) {
        if (CollectionUtil.isEmpty(dynamicClassConfigList)) {
            return;
        }
        List<DynamicClassEntity> resultList = new ArrayList<>(dynamicClassConfigList.size());
        for (ConfigService.DynamicClassConfiguration config : dynamicClassConfigList) {
            resultList.add(new DynamicClassEntity(config.getFullClassName(),
                config.getMethodName(), config.getParameterTypes(), config.getKeyFormula()));
        }
        this.dynamicClassList = resultList;
    }

    @VisibleForTesting
    void init() {
        agentVersion = System.getProperty(AGENT_VERSION, "0.2.0");
        setEnableDebug(System.getProperty(ENABLE_DEBUG));
        setServiceName(StringUtils.strip(System.getProperty(SERVICE_NAME)));
        setStorageServiceHost(StringUtils.strip(System.getProperty(STORAGE_SERVICE_HOST)));
        configPath = StringUtils.strip(System.getProperty(CONFIG_PATH));
        setRecordRate(System.getProperty(RECORD_RATE, "1"));

        storageServiceMode = System.getProperty(STORAGE_SERVICE_MODE);

        setDynamicResultSizeLimit(System.getProperty(DYNAMIC_RESULT_SIZE_LIMIT, "1000"));
        setTimeMachine(System.getProperty(TIME_MACHINE));
        setAllowDayOfWeeks(Integer.parseInt(System.getProperty(ALLOW_DAY_WEEKS, "127")));
        setAllowTimeOfDayFrom(System.getProperty(ALLOW_TIME_FROM, "00:01"));
        setAllowTimeOfDayTo(System.getProperty(ALLOW_TIME_TO, "23:59"));
        setDisabledInstrumentationModules(System.getProperty(DISABLE_INSTRUMENTATION_MODULE));
        setExcludeServiceOperations(System.getProperty(EXCLUDE_SERVICE_OPERATION));
        setDubboStreamReplayThreshold(System.getProperty(DUBBO_STREAM_REPLAY_THRESHOLD, "100"));
        setDisableReplay(System.getProperty(DISABLE_REPLAY));

        TimerService.scheduleAtFixedRate(this::update, 120, 120, TimeUnit.SECONDS);
    }

    private void updateInstrumentationConfig() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put(DYNAMIC_RESULT_SIZE_LIMIT, String.valueOf(getDynamicResultSizeLimit()));
        configMap.put(TIME_MACHINE, String.valueOf(startTimeMachine()));
        configMap.put(DISABLE_REPLAY, String.valueOf(disableReplay()));
        configMap.put(DURING_WORK, Boolean.toString(nextWorkTime() <= 0));
        configMap.put(AGENT_VERSION, agentVersion);
        Map<String, String> extendFieldMap = getExtendField();
        if (extendFieldMap != null && !extendFieldMap.isEmpty()) {
            configMap.putAll(extendFieldMap);
        }

        ConfigBuilder.create(getServiceName())
            .enableDebug(isEnableDebug())
            .addProperties(configMap)
            .dynamicClassList(getDynamicClassList())
            .excludeServiceOperations(getExcludeServiceOperations())
            .dubboStreamReplayThreshold(getDubboStreamReplayThreshold())
            .recordRate(getRecordRate())
            .build();
    }

    @VisibleForTesting
    void readConfigFromFile(String configPath) {
        if (StringUtil.isEmpty(configPath)) {
            LOGGER.info("arex agent config path is null");
            return;
        }

        Map<String, String> configMap = parseConfigFile(configPath);
        if (configMap.size() == 0) {
            LOGGER.info("arex agent config is empty");
            return;
        }

        setEnableDebug(configMap.get(ENABLE_DEBUG));
        setServiceName(configMap.get(SERVICE_NAME));
        setStorageServiceHost(configMap.get(STORAGE_SERVICE_HOST));
        setRecordRate(configMap.get(RECORD_RATE));
        setDynamicResultSizeLimit(configMap.get(DYNAMIC_RESULT_SIZE_LIMIT));
        setTimeMachine(configMap.get(TIME_MACHINE));
        setStorageServiceMode(configMap.get(STORAGE_SERVICE_MODE));
        setDisabledInstrumentationModules(configMap.get(DISABLE_INSTRUMENTATION_MODULE));
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
                String value = StringUtils.strip(item.substring(separatorIndex + 1));
                configMap.put(key, value);
            });
        } catch (IOException e) {
            LOGGER.warn("Parse config file failed", e);
        }

        return configMap;
    }

    private void update() {
        ConfigService.INSTANCE.loadAgentConfig(null);
    }

    public boolean isLocalStorage() {
        return STORAGE_MODE.equalsIgnoreCase(storageServiceMode);
    }

    public void setStorageServiceMode(String storageServiceMode) {
        this.storageServiceMode = storageServiceMode;
    }

    public void parseAgentConfig(String args) {
        Map<String, String> agentMap = StringUtil.asMap(args);
        if (!agentMap.isEmpty()) {
            String mode = agentMap.get(STORAGE_SERVICE_MODE);
            if (StringUtil.isNotEmpty(mode)) {
                storageServiceMode = mode;
            }
            setEnableDebug(agentMap.get(ENABLE_DEBUG));
            updateInstrumentationConfig();
        }
    }

    public int getRecordRate() {
        return recordRate;
    }

    public List<DynamicClassEntity> getDynamicClassList() {
        return dynamicClassList;
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

    public void parseServiceConfig(ConfigService.ResponseBody serviceConfig) {
        ConfigService.ServiceCollectConfig config = serviceConfig.getServiceCollectConfiguration();
        setRecordRate(String.valueOf(config.getSampleRate()));
        setTimeMachine(String.valueOf(config.isTimeMock()));
        setAllowDayOfWeeks(config.getAllowDayOfWeeks());
        setAllowTimeOfDayFrom(config.getAllowTimeOfDayFrom());
        setAllowTimeOfDayTo(config.getAllowTimeOfDayTo());
        setDynamicClassList(serviceConfig.getDynamicClassConfigurationList());
        setExcludeServiceOperations(config.getExcludeServiceOperationSet());
        setTargetAddress(serviceConfig.getTargetAddress());
        setExtendField(serviceConfig.getExtendField());

        updateInstrumentationConfig();
    }

    public boolean invalid() {
        if (isLocalStorage()) {
            return false;
        }
        return !checkTargetAddress();
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

    public List<String> getDisabledInstrumentationModules() {
        return disabledInstrumentationModules;
    }

    public void setDisabledInstrumentationModules(String disabledInstrumentationModules) {
        if (StringUtil.isEmpty(disabledInstrumentationModules)) {
            if (this.disabledInstrumentationModules == null) {
                this.disabledInstrumentationModules = Collections.emptyList();
            }
            return;
        }

        this.disabledInstrumentationModules = new ArrayList<>(
            Arrays.asList(StringUtil.split(disabledInstrumentationModules, ',')));
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
        if (StringUtil.isEmpty(targetAddress) || StringUtil.isEmpty(localHost)) {
            return true;
        }
        return targetAddress.equals(localHost);
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
