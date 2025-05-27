package io.arex.agent.bootstrap.model;

/**
 * MockDataResponseHead
 *
 * @author ywqiu
 * @date 2025/4/16 10:06
 */
public class MockDataResponseHead {

    private String transactionID;
    private String serverIP;

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public String getServerIP() {
        return serverIP;
    }
}
