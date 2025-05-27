package io.arex.foundation.model;

/**
 * NextBuilderMockDataQueryRequest
 *
 * @author ywqiu
 * @date 2025/4/16 9:51
 */
public class NextBuilderMockDataQueryRequest {

    private MockDataHead requestHead;
    private String env;
    private String serviceUrl;
    private String requestContent;
    private String messageId;
    private String preciseQuery;
    private String txId;
    private String traceId;
    private String parentTraceId;
    private String requestType;

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setParentTraceId(String parentTraceId) {
        this.parentTraceId = parentTraceId;
    }

    public String getTxId() {
        return txId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getParentTraceId() {
        return parentTraceId;
    }

    public void setRequestHead(MockDataHead requestHead) {
        this.requestHead = requestHead;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void setRequestContent(String requestContent) {
        this.requestContent = requestContent;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setPreciseQuery(String preciseQuery) {
        this.preciseQuery = preciseQuery;
    }

    public MockDataHead getRequestHead() {
        return requestHead;
    }

    public String getEnv() {
        return env;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getRequestContent() {
        return requestContent;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getPreciseQuery() {
        return preciseQuery;
    }
}
