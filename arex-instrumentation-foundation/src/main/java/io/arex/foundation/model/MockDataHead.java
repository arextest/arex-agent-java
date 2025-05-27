package io.arex.foundation.model;

/**
 * MockDataHead
 *
 * @author ywqiu
 * @date 2025/4/16 9:52
 */
public class MockDataHead {

    private String transactionID;
    private String source;
    private String appId;
    private String sessionID;
    private String clientIP;

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public String getSource() {
        return source;
    }

    public String getAppId() {
        return appId;
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getClientIP() {
        return clientIP;
    }
}
