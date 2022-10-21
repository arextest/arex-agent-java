package io.arex.foundation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.function.Predicate;

/**
 * ServletMocker
 *
 * @date 2022/02/16
 */
public class ServiceEntranceMocker extends AbstractMocker {
    @JsonProperty("method")
    private String method;
    @JsonProperty("path")
    private String path;
    @JsonProperty("pattern")
    private String pattern;
    @JsonProperty("requestHeaders")
    private String requestHeaders;
    @JsonProperty("responseHeaders")
    private String responseHeaders;
    @JsonProperty("request")
    private String request;


    @SuppressWarnings("deserilize")
    public ServiceEntranceMocker() {
        super(MockerCategory.SERVLET_ENTRANCE);
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

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    @Override
    protected Predicate<ServiceEntranceMocker> filterLocalStorage() {
        return mocker -> {
            if (StringUtils.isNotBlank(path) && !StringUtils.equals(path, mocker.getPath())) {
                return false;
            }
            if (StringUtils.isNotBlank(request) && !StringUtils.equals(request, mocker.getRequest())) {
                return false;
            }
            return true;
        };
    }

    @Override
    public Object parseMockResponse(AbstractMocker requestMocker) {
        return new ServiceEntranceMocker();
    }
}