package io.arex.agent.bootstrap.model;


/**
 * MockResult
 *
 * @author ywqiu
 * @date 2025/8/27 9:49
 */
public class NextBuilderMockResult {

    private String data;
    private String sessionId;
    private String acceptEncoding;

    public String getData() {
        return data;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAcceptEncoding() {
        return acceptEncoding;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setAcceptEncoding(String acceptEncoding) {
        this.acceptEncoding = acceptEncoding;
    }
}
