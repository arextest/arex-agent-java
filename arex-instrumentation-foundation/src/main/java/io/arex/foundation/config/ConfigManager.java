package io.arex.foundation.config;

import com.google.common.annotations.VisibleForTesting;
import io.arex.foundation.model.DynamicClassEntity;
import io.arex.foundation.services.TimerService;
import io.arex.foundation.util.PropertyUtil;
import io.arex.foundation.util.StringUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
    private static final String CONFIG_PATH = "arex.config.path";
    private static final String STORAGE_MODE = "local";
    private static final String RECORD_RATE = "arex.rate.limit";
    private static final String DYNAMIC_CLASS_KEY = "arex.dynamic.class";
    private static final String DYNAMIC_RESULT_SIZE_LIMIT = "arex.dynamic.result.size.limit";
    private static final String TIME_MACHINE = "arex.time.machine";
    private static final String STORAGE_SERVICE_MODE = "arex.storage.mode";
    private static final String STORAGE_SERVICE_JDBC_URL = "arex.storage.jdbc.url";
    private static final String STORAGE_SERVICE_USER_NAME = "arex.storage.username";
    private static final String STORAGE_SERVICE_PASSWORD = "arex.storage.password";
    private static final String STORAGE_SERVICE_WEB_PORT = "arex.storage.web.port";
    private static final String SERVER_SERVICE_TCP_PORT = "arex.server.tcp.port";

    private boolean enableDebug;
    private String agentVersion;
    private String serviceName;
    private String storageServiceHost;
    private String configPath;

    private String storageServiceMode;
    private String storageServiceJdbcUrl;
    private String storageServiceUsername;
    private String storageServicePassword;
    private String storageServiceWebPort;
    private String serverServiceTcpPort;
    private int recordRate;
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

    public void setRecordRate(String recordRate) {
        if (StringUtil.isEmpty(recordRate)) {
            return;
        }
        this.recordRate = Integer.parseInt(recordRate);
        System.setProperty(RECORD_RATE, recordRate);
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

    public void setDynamicClassList(String dynamicClass) {
        if (StringUtil.isEmpty(dynamicClass)) {
            return;
        }
        this.dynamicClassList = parseDynamicClassList(dynamicClass);
        System.setProperty(DYNAMIC_CLASS_KEY, dynamicClass);
    }

    @VisibleForTesting
    void init() {
        agentVersion = "0.0.1";
        enableDebug = Boolean.parseBoolean(System.getProperty(ENABLE_DEBUG));
        serviceName = StringUtils.strip(System.getProperty(SERVICE_NAME));
        storageServiceHost = StringUtils.strip(System.getProperty(STORAGE_SERVICE_HOST));
        configPath = StringUtils.strip(System.getProperty(CONFIG_PATH));
        recordRate = Integer.parseInt(System.getProperty(RECORD_RATE, "1"));

        storageServiceMode = System.getProperty(STORAGE_SERVICE_MODE);
        storageServiceJdbcUrl = System.getProperty(STORAGE_SERVICE_JDBC_URL, PropertyUtil.getProperty(STORAGE_SERVICE_JDBC_URL));
        storageServiceUsername = System.getProperty(STORAGE_SERVICE_USER_NAME, PropertyUtil.getProperty(STORAGE_SERVICE_USER_NAME));
        storageServicePassword = System.getProperty(STORAGE_SERVICE_PASSWORD, PropertyUtil.getProperty(STORAGE_SERVICE_PASSWORD));
        storageServiceWebPort = System.getProperty(STORAGE_SERVICE_WEB_PORT, PropertyUtil.getProperty(STORAGE_SERVICE_WEB_PORT));
        serverServiceTcpPort = System.getProperty(SERVER_SERVICE_TCP_PORT, PropertyUtil.getProperty(SERVER_SERVICE_TCP_PORT));

        dynamicClass = System.getProperty(DYNAMIC_CLASS_KEY);
        dynamicClassList = parseDynamicClassList(dynamicClass);
        dynamicResultSizeLimit = Integer.parseInt(System.getProperty(DYNAMIC_RESULT_SIZE_LIMIT, "1000"));

        startTimeMachine = BooleanUtils.toBoolean(System.getProperty(TIME_MACHINE, Boolean.FALSE.toString()));

        TimerService.scheduleAtFixedRate(ConfigManager::update, 300, 300, TimeUnit.SECONDS);
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
        setDynamicClassList(configMap.get(DYNAMIC_CLASS_KEY));
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
            String mode = agentMap.get(STORAGE_SERVICE_MODE);
            if (StringUtil.isNotEmpty(mode)) {
                storageServiceMode = mode;
            }
            String url = agentMap.get(STORAGE_SERVICE_JDBC_URL);
            if (StringUtil.isNotEmpty(url)) {
                storageServiceJdbcUrl = url;
            }
            String userName = agentMap.get(STORAGE_SERVICE_USER_NAME);
            if (StringUtil.isNotEmpty(userName)) {
                storageServiceUsername = userName;
            }
            String password = agentMap.get(STORAGE_SERVICE_PASSWORD);
            if (StringUtil.isNotEmpty(password)) {
                storageServicePassword = password;
            }
            String webPort = agentMap.get(STORAGE_SERVICE_WEB_PORT);
            if (StringUtil.isNotEmpty(webPort)) {
                storageServiceWebPort = webPort;
            }
            String tcpPort = agentMap.get(SERVER_SERVICE_TCP_PORT);
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
