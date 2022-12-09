package io.arex.inst.httpclient.okhttp.v3;


import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.httpclient.common.HttpResponseWrapper.StringTuple;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Response.Builder;
import okhttp3.ResponseBody;
import okhttp3.internal.http.StatusLine;
import okio.Buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

public class OkHttpClientAdapter implements HttpClientAdapter<Request, MockResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OkHttpClientAdapter.class);
    private final Request httpRequest;

    public OkHttpClientAdapter(Request httpRequest) {
        this.httpRequest = httpRequest;
    }

    @Override
    public String getMethod() {
        return httpRequest.method();
    }

    @Override
    public String getRequestContentType() {
        final RequestBody requestBody = httpRequest.body();
        MediaType mediaType = null;
        if (requestBody != null) {
            mediaType = requestBody.contentType();
        }
        return mediaType == null ? null : mediaType.toString();
    }

    @Override
    public String getRequestHeader(String name) {
        return this.httpRequest.header(name);
    }

    @Override
    public URI getUri() {
        return this.httpRequest.url().uri();
    }

    @Override
    public HttpResponseWrapper wrap(MockResult mockResult) {
        Response response = (Response) mockResult.getResult();
        HttpResponseWrapper wrapper = new HttpResponseWrapper();
        StatusLine statusLine = new StatusLine(response.protocol(), response.code(), response.message());
        wrapper.setStatusLine(statusLine.toString());
        try {
            ResponseBody responseBody = response.peekBody(Long.MAX_VALUE);
            wrapper.setContent(responseBody.bytes());
        } catch (IOException e) {
            LOGGER.warn("encode response error:{}", e.getMessage(), e);
        }
        Headers headers = response.headers();
        wrapper.setHeaders(encodeHeaders(headers));
        return wrapper;
    }

    private List<StringTuple> encodeHeaders(Headers headers) {
        if (headers == null || headers.size() == 0) {
            return Collections.emptyList();
        }
        List<StringTuple> encodeHeaders = new ArrayList<>(headers.size());
        for (int i = 0; i < headers.size(); i++) {
            if (StringUtil.isEmpty(headers.name(i))) {
                continue;
            }
            encodeHeaders.add(new StringTuple(headers.name(i), headers.value(i)));
        }
        return encodeHeaders;
    }

    @Override
    public MockResult unwrap(HttpResponseWrapper wrapped) {
        Response.Builder responseBuilder = new Builder();
        Headers.Builder headersBuilder = new Headers.Builder();
        List<StringTuple> headers = wrapped.getHeaders();
        String contentType = null;
        StringTuple stringTuple;
        for (int i = 0; i < headers.size(); i++) {
            stringTuple = headers.get(i);
            headersBuilder.add(stringTuple.name(), stringTuple.value());

            if (stringTuple.name().equalsIgnoreCase(CONTENT_TYPE_NAME)) {
                contentType = stringTuple.value();
            }
        }
        ResponseBody responseBody = null;
        if (contentType != null) {
            responseBody = ResponseBody.create(wrapped.getContent(), MediaType.get(contentType));
        }
        responseBuilder.request(this.httpRequest);
        responseBuilder.body(responseBody);
        responseBuilder.headers(headersBuilder.build());
        try {
            StatusLine statusLine = StatusLine.Companion.parse(wrapped.getStatusLine());
            responseBuilder.code(statusLine.code);
            responseBuilder.message(statusLine.message);
            responseBuilder.protocol(statusLine.protocol);
        } catch (IOException e) {
            LOGGER.warn("decode response StatusLine error:{}", e.getMessage(), e);
        }
        return MockResult.success(wrapped.isIgnoreMockResult(), responseBuilder.build());
    }

    @Override
    public byte[] getRequestBytes() {
        final RequestBody body = this.httpRequest.body();
        if (body == null) {
            return null;
        }
        try {
            final Buffer buffer = new Buffer();
            body.writeTo(buffer);
            return buffer.readByteArray();
        } catch (IOException e) {
            LOGGER.warn("copy request body to base64 error:{}", e.getMessage(), e);
        }
        return null;
    }
}