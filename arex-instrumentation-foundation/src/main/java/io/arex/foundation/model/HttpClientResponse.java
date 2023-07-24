package io.arex.foundation.model;

import java.util.Map;

public class HttpClientResponse {
    private int statusCode;
    private Map<String, String> headers;
    private String body;

    public HttpClientResponse() {
    }

    public HttpClientResponse(int status, Map<String, String> headers, String body) {
        this.statusCode = status;
        this.headers = headers;
        this.body = body;
    }

    public static HttpClientResponse emptyResponse() {
        return new HttpClientResponse(0, null, null);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
