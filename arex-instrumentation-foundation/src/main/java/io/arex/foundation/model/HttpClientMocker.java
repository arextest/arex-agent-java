package io.arex.foundation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public class HttpClientMocker extends AbstractMocker {
    @JsonProperty("url")
    private String url;
    @JsonProperty("contentType")
    private String contentType;
    @JsonProperty("request")
    private String request;
    @JsonProperty("method")
    private String method;

    public HttpClientMocker() {
        super(MockerCategory.SERVICE_CALL);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public String getRequest() {
        return request;
    }

    @Override
    protected Predicate<HttpClientMocker> filterLocalStorage() {
        return mocker -> {
            if (StringUtils.isNotBlank(url) && !StringUtils.equals(url, mocker.getUrl())) {
                return false;
            }
            if (StringUtils.isNotBlank(request) && !StringUtils.equals(request, mocker.getRequest())) {
                return false;
            }
            return true;
        };
    }
}