package io.arex.inst.httpclient.common;

import java.net.URI;

public interface HttpClientAdapter<TRequest, TResponse> {
    byte[] ZERO_BYTE = new byte[0];
    String CONTENT_TYPE_NAME = "Content-Type";
    String USER_AGENT_NAME = "User-Agent";

    String getMethod();

    byte[] getRequestBytes();

    String getRequestContentType();

    String getRequestHeader(String name);

    URI getUri();

    HttpResponseWrapper wrap(TResponse response);

    TResponse unwrap(HttpResponseWrapper wrapped);
}