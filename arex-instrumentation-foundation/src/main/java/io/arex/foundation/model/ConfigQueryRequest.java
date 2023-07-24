package io.arex.foundation.model;

import java.util.Map;

public class ConfigQueryRequest {
    private String appId;
    private String recordVersion;
    private String host;
    /**
     * {@link AgentStatusEnum}
     */
    private String agentStatus;
    private Map<String, String> systemProperties;
    private Map<String, String> systemEnv;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getRecordVersion() {
        return recordVersion;
    }

    public void setRecordVersion(String recordVersion) {
        this.recordVersion = recordVersion;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAgentStatus() {
        return agentStatus;
    }

    public void setAgentStatus(String agentStatus) {
        this.agentStatus = agentStatus;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    public void setSystemEnv(Map<String, String> systemEnv) {
        this.systemEnv = systemEnv;
    }

    public Map<String, String> getSystemEnv() {
        return systemEnv;
    }
}
