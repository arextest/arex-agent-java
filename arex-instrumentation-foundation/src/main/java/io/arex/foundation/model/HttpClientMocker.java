package io.arex.foundation.model;

import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.TypeUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    public HttpClientMocker(){}

    public HttpClientMocker(String target, String contentType, String request) {
        this(target, contentType, request, null);
    }

    public HttpClientMocker(String target, String contentType, String request, Object response) {
        super();

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
    public int getCategoryType() {
        return 3;
    }

    @Override
    public String getCategoryName() {
        return "ServiceCall";
    }

    @Override
    public Object parseMockResponse() {
        return response;
    }
}
