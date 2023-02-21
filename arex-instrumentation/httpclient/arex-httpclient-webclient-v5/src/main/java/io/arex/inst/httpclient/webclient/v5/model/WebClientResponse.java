package io.arex.inst.httpclient.webclient.v5.model;

import org.springframework.web.reactive.function.client.ClientResponse;

public class WebClientResponse {
    private ClientResponse response;
    private byte[] content;
    private WebClientResponse(ClientResponse response, byte[] content) {
        this.response = response;
        this.content = content;
    }
    public static WebClientResponse of(ClientResponse defaultResponse) {
        return of(defaultResponse, null);
    }
    public static WebClientResponse of(ClientResponse response, byte[] content) {
        return new WebClientResponse(response, content);
    }
    public ClientResponse originalResponse() {
        return response;
    }
    public byte[] getContent() {
        return content;
    }

}
