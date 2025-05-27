package io.arex.agent.bootstrap.model;

/**
 * NextBuilderMock
 *
 * @author ywqiu
 * @date 2025/4/24 10:51
 */
public class NextBuilderMock {

    private String serviceUrl;
    private String originRequestBody;
    private String transactionId;
    private String requestMethod;
    private String clientIP;
    private String subEnv;
    private String messageId;
    private String parentMessageId;

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void setOriginRequestBody(String originRequestBody) {
        this.originRequestBody = originRequestBody;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public void setSubEnv(String subEnv) {
        this.subEnv = subEnv;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setParentMessageId(String parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getOriginRequestBody() {
        return originRequestBody;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getClientIP() {
        return clientIP;
    }

    public String getSubEnv() {
        return subEnv;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getParentMessageId() {
        return parentMessageId;
    }
}
