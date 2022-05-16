package io.arex.foundation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * ServletMocker
 *
 *
 * @date 2022/02/16
 */
public class ServletMocker extends AbstractMocker {
    @JsonProperty("method")
    private String method;
    @JsonProperty("path")
    private String path;
    @JsonProperty("pattern")
    private String pattern;
    @JsonProperty("requestHeaders")
    private Map<String, String> requestHeaders;
    @JsonProperty("responseHeaders")
    private Map<String, String> responseHeaders;
    @JsonProperty("request")
    private String request;
    @JsonProperty("response")
    private String response;

    @SuppressWarnings("deserilize")
    public ServletMocker() {
        super();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }


    @Override
    public int getCategoryType() {
        return 1;
    }

    @Override
    public String getCategoryName() {
        return "ServletEntrance";
    }

    @Override
    public Object parseMockResponse() {
        return response;
    }
}
