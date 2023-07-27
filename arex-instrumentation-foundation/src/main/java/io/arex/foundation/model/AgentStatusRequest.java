package io.arex.foundation.model;

import java.io.Serializable;

public class AgentStatusRequest implements Serializable {

    private String appId;
    private String host;
    /**
     * see {@link AgentStatusEnum}
     */
    private String agentStatus;

    public AgentStatusRequest() {
    }

    public AgentStatusRequest(String appId, String ip, String agentStatus) {
        this.appId = appId;
        this.host = ip;
        this.agentStatus = agentStatus;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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
}
