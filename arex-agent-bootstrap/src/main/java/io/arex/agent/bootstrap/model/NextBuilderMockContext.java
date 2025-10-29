package io.arex.agent.bootstrap.model;

/**
 * NextBuilderMockContext
 *
 * @author ywqiu
 * @date 2025/4/22 14:31
 */
public class NextBuilderMockContext {

    private String url;
    private String body;
    private String requestMethod;
    private String mockResponseBody;
    private String acceptEncoding;
    private Object soaMockResponse;
    private boolean interruptOriginalRequest;

    public boolean isInterruptOriginalRequest() {
        return interruptOriginalRequest;
    }

    public void setInterruptOriginalRequest(boolean interruptOriginalRequest) {
        this.interruptOriginalRequest = interruptOriginalRequest;
    }

    public String getMockResponseBody() {
        return mockResponseBody;
    }

    public void setMockResponseBody(String mockResponseBody) {
        this.mockResponseBody = mockResponseBody;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAcceptEncoding() {
        return acceptEncoding;
    }

    public void setAcceptEncoding(String acceptEncoding) {
        this.acceptEncoding = acceptEncoding;
    }

    public Object getSoaMockResponse() {
        return soaMockResponse;
    }

    public void setSoaMockResponse(Object soaMockResponse) {
        this.soaMockResponse = soaMockResponse;
    }
}
