package io.arex.foundation.model;

import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.TypeUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public class HttpClientMocker extends AbstractMocker {
    @JsonProperty("url")
    private String url;
    @JsonProperty("response")
    private String response;
    @JsonProperty("responseType")
    private String responseType;
    @JsonProperty("contentType")
    private String contentType;
    @JsonProperty("request")
    private String request;

    @SuppressWarnings("deserialize")
    public HttpClientMocker(){
        super(MockerCategory.SERVICE_CALL);
    }

    public HttpClientMocker(String target, String contentType, String request) {
        this(target, contentType, request, null);
    }

    public HttpClientMocker(String target, String contentType, String request, Object response) {
        super(MockerCategory.SERVICE_CALL);

        this.contentType = contentType;
        this.request = request;
        this.url = target;
        if (response != null) {
            this.response = SerializeUtils.serialize(response);
            this.responseType = TypeUtil.getName(response);
        }
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public Object parseMockResponse(AbstractMocker requestMocker) {
        return response;
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
