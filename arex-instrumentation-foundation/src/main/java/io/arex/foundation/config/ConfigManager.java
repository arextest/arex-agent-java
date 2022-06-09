package io.arex.foundation.config;

import io.arex.foundation.model.DynamicClassEntity;
import io.arex.foundation.services.TimerService;
import io.arex.foundation.util.PropertyUtil;
import io.arex.foundation.util.StringUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    public static final ConfigManager INSTANCE = new ConfigManager();

    private static final String ENABLE_DEBUG = "arex.enable.debug";
    private static final String SERVICE_NAME = "arex.service.name";
    private static final String STORAGE_SERVICE_HOST = "arex.storage.service.host";
    private static final String CONFIG_SERVICE_HOST = "arex.config.service.host";
    private static final String CONFIG_PATH = "arex.config.path";
    private static final String STORAGE_MODE = "local";
    private static final String RECORD_RATE = "arex.rate.limit";
    private static final String FORCE_RECORD = "arex.force.record";
    private static final String DYNAMIC_CLASS_KEY = "arex.dynamic.class";
    private static final String DYNAMIC_RESULT_SIZE_LIMIT = "arex.dynamic.result.size.limit";
    private static final String TIME_MACHINE = "arex.time.machine";

    private boolean enableDebug;
    private String agentVersion;
    private String serviceName;
    private String storageServiceHost;
    private String configServiceHost;
    private String configPath;

    private String storageServiceMode;
    private String storageServiceJdbcUrl;
    private String storageServiceUsername;
    private String storageServicePassword;
    private String storageServiceWebPort;
    private String serverServiceTcpPort;
    private int recordRate;
    private boolean forceRecord;
    private String dynamicClass;
    private int dynamicResultSizeLimit;
    private List<DynamicClassEntity> dynamicClassList;
    /**
     * use only replay
     */
    private boolean startTimeMachine;

    private static List<ConfigListener> listeners = new ArrayList<ConfigListener>();

    private ConfigManager() {
        init();
        readConfigFromFile(configPath);
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
        return configServiceHost;
    }

    public void setConfigServiceHost(String configServiceHost) {
        if (StringUtil.isEmpty(configServiceHost)) {
            return;
        }
        this.configServiceHost = configServiceHost;
        System.setProperty(CONFIG_SERVICE_HOST, configServiceHost);
    }

    public void setRecordRate(String recordRate) {
        if (StringUtil.isEmpty(recordRate)) {
            return;
        }
        this.recordRate = Integer.parseInt(recordRate);
        System.setProperty(RECORD_RATE, recordRate);
    }

    public void setForceRecord(String forceRecord) {
        if (StringUtil.isEmpty(forceRecord)) {
            return;
        }
        this.forceRecord = BooleanUtils.toBoolean(forceRecord);
        System.setProperty(FORCE_RECORD, forceRecord);
    }

    public void setDynamicResultSizeLimit(String dynamicResultSizeLimit) {
        if (StringUtil.isEmpty(dynamicResultSizeLimit)) {
            return;
        }
        this.dynamicResultSizeLimit = Integer.parseInt(dynamicResultSizeLimit);
        System.setProperty(DYNAMIC_RESULT_SIZE_LIMIT, dynamicResultSizeLimit);
    }

    public void setTimeMachine(String timeMachine) {
        if (StringUtil.isEmpty(timeMachine)) {
            return;
        }
        this.startTimeMachine = BooleanUtils.toBoolean(timeMachine);
        System.setProperty(TIME_MACHINE, timeMachine);
    }

    private void init() {
        agentVersion = "0.0.1";
        enableDebug = Boolean.parseBoolean(System.getProperty(ENABLE_DEBUG));
        serviceName = System.getProperty(SERVICE_NAME);
        storageServiceHost = System.getProperty(STORAGE_SERVICE_HOST);
        configServiceHost = System.getProperty(CONFIG_SERVICE_HOST);
        configPath = System.getProperty(CONFIG_PATH);
        recordRate = Integer.parseInt(System.getProperty(RECORD_RATE, "1"));
        forceRecord = BooleanUtils.toBoolean(System.getProperty(FORCE_RECORD, Boolean.FALSE.toString()));

        storageServiceMode = System.getProperty("arex.storage.mode");
        storageServiceJdbcUrl = System.getProperty("arex.storage.jdbc.url", PropertyUtil.getProperty("arex.storage.jdbc.url"));
        storageServiceUsername = System.getProperty("arex.storage.username", PropertyUtil.getProperty("arex.storage.username"));
        storageServicePassword = System.getProperty("arex.storage.password", PropertyUtil.getProperty("arex.storage.password"));
        storageServiceWebPort = System.getProperty("arex.storage.web.port", PropertyUtil.getProperty("arex.storage.web.port"));
        serverServiceTcpPort = System.getProperty("arex.server.tcp.port", PropertyUtil.getProperty("arex.server.tcp.port"));

        dynamicClass = System.getProperty(DYNAMIC_CLASS_KEY);
        dynamicClassList = parseDynamicClassList(dynamicClass);
        dynamicResultSizeLimit = Integer.parseInt(System.getProperty(DYNAMIC_RESULT_SIZE_LIMIT, "1000"));

        startTimeMachine = BooleanUtils.toBoolean(System.getProperty(TIME_MACHINE, Boolean.FALSE.toString()));

        TimerService.scheduleAtFixedRate(ConfigManager::update, 300, 300, TimeUnit.SECONDS);
    }

    public void readConfigFromFile(String configPath) {
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
        setConfigServiceHost(configMap.get(CONFIG_SERVICE_HOST));
        setRecordRate(configMap.get(RECORD_RATE));
        setForceRecord(configMap.get(FORCE_RECORD));
        setDynamicResultSizeLimit(configMap.get(DYNAMIC_RESULT_SIZE_LIMIT));
        setTimeMachine(configMap.get(TIME_MACHINE));
    }

    private static Map<String, String> parseConfigFile(String configPath) {
        Map<String, String> configMap = new HashMap<>();
        try {
            Stream<String> configStream = Files.lines(Paths.get(configPath));
            configStream.forEach(item-> {
                int separatorIndex = item.indexOf('=');
                if (separatorIndex < 0) {
                    return;
                }
                String key = item.substring(0, separatorIndex);
                String value = item.substring(separatorIndex + 1);
                configMap.put(key, value);
            });
        } catch (IOException e) {
            LOGGER.warn("Parse config file failed", e);
        }

        return configMap;
    }

    private static void start() {

    }

    private static void update() {

    }

    public String getStorageServiceMode() {
        return storageServiceMode;
    }

    public String getStorageServiceJdbcUrl() {
        return storageServiceJdbcUrl;
    }

    public String getStorageServiceUsername() {
        return storageServiceUsername;
    }

    public String getStorageServicePassword() {
        return storageServicePassword;
    }

    public boolean isLocalStorage(){
        return STORAGE_MODE.equalsIgnoreCase(storageServiceMode);
    }

    public void setStorageServiceMode(String storageServiceMode) {
        this.storageServiceMode = storageServiceMode;
    }

    public void setStorageServiceJdbcUrl(String storageServiceJdbcUrl) {
        this.storageServiceJdbcUrl = storageServiceJdbcUrl;
    }

    public void setStorageServiceUsername(String storageServiceUsername) {
        this.storageServiceUsername = storageServiceUsername;
    }

    public void setStorageServicePassword(String storageServicePassword) {
        this.storageServicePassword = storageServicePassword;
    }

    public String getStorageServiceWebPort() {
        return storageServiceWebPort;
    }

    public String getServerServiceTcpPort() {
        return serverServiceTcpPort;
    }

    public void parseAgentConfig(String args) {
        Map<String, String> agentMap = StringUtil.asMap(args);
        if (agentMap != null && agentMap.size() > 0) {
            String mode = agentMap.get("arex.storage.mode");
            if (StringUtil.isNotEmpty(mode)) {
                storageServiceMode = mode;
            }
            String url = agentMap.get("arex.storage.jdbc.url");
            if (StringUtil.isNotEmpty(url)) {
                storageServiceJdbcUrl = url;
            }
            String userName = agentMap.get("arex.storage.username");
            if (StringUtil.isNotEmpty(userName)) {
                storageServiceUsername = userName;
            }
            String password = agentMap.get("arex.storage.password");
            if (StringUtil.isNotEmpty(password)) {
                storageServicePassword = password;
            }
            String webPort = agentMap.get("arex.storage.web.port");
            if (StringUtil.isNotEmpty(webPort)) {
                storageServiceWebPort = webPort;
            }
            String tcpPort = agentMap.get("arex.server.tcp.port");
            if (StringUtil.isNotEmpty(tcpPort)) {
                serverServiceTcpPort = tcpPort;
            }
        }
    }

    private List<DynamicClassEntity> parseDynamicClassList(String dynamicClassValue) {
        if (StringUtil.isEmpty(dynamicClassValue)) {
            return Collections.emptyList();
        }

        String[] array = dynamicClassValue.split(";");

        if (array.length < 1) {
            return Collections.emptyList();
        }

        List<DynamicClassEntity> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            String[] subArray = array[i].split("#");
            if (subArray.length < 3) {
                continue;
            }

            String fullClassName = subArray[0];
            String methodName = subArray[1];
            String parameterTypes = subArray[2];

            if (StringUtil.isEmpty(fullClassName) || StringUtil.isEmpty(methodName) || StringUtil.isEmpty(parameterTypes)) {
                continue;
            }

            list.add(new DynamicClassEntity(fullClassName, methodName, parameterTypes));
        }
        return list;
    }

    public int getRecordRate() {
        return recordRate;
    }

    public void setRecordRate(int recordRate) {
        this.recordRate = recordRate;
    }

    public boolean isForceRecord() {
        return forceRecord;
    }

    public void setForceRecord(boolean forceRecord) {
        this.forceRecord = forceRecord;
    }

    public List<DynamicClassEntity> getDynamicClassList() {
        return dynamicClassList;
    }

    public void setDynamicClassList(List<DynamicClassEntity> dynamicClassList) {
        this.dynamicClassList = dynamicClassList;
    }

    public int getDynamicResultSizeLimit() {
        return dynamicResultSizeLimit;
    }

    public void setDynamicResultSizeLimit(int dynamicResultSizeLimit) {
        this.dynamicResultSizeLimit = dynamicResultSizeLimit;
    }

    public boolean startTimeMachine() {
        return startTimeMachine;
    }

    @Override
    public String toString() {
        return "ConfigManager{" +
                "enableDebug=" + enableDebug +
                ", agentVersion='" + agentVersion + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", storageServiceHost='" + storageServiceHost + '\'' +
                ", configServiceHost='" + configServiceHost + '\'' +
                ", configPath='" + configPath + '\'' +
                ", storageServiceMode='" + storageServiceMode + '\'' +
                ", storageServiceJdbcUrl='" + storageServiceJdbcUrl + '\'' +
                ", storageServiceUsername='" + storageServiceUsername + '\'' +
                ", storageServicePassword='" + storageServicePassword + '\'' +
                ", storageServiceWebPort='" + storageServiceWebPort + '\'' +
                ", serverServiceTcpPort='" + serverServiceTcpPort + '\'' +
                '}';
    }
}
