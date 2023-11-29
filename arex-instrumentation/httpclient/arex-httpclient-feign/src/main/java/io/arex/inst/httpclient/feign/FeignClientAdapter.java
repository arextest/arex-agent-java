package io.arex.inst.httpclient.feign;

import feign.Request;
import feign.Response;
import feign.Response.Body;
import feign.Util;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.httpclient.common.HttpResponseWrapper.StringTuple;
import io.arex.inst.runtime.log.LogManager;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeignClientAdapter implements HttpClientAdapter<Request, Response> {
    private static final String CONTENT_TYPE = "Content-Type";
    private final Request request;
    private final URI uri;
    private byte[] responseBody;

    public FeignClientAdapter(Request request, URI uri) {
        this.request = request;
        this.uri = uri;
    }

    @Override
    public String getMethod() {
        return request.method();
    }

    @Override
    public byte[] getRequestBytes() {
        return request.body();
    }

    @Override
    public String getRequestContentType() {
        return getRequestHeader(CONTENT_TYPE);
    }

    @Override
    public String getRequestHeader(String name) {
        final Collection<String> values = request.headers().get(name);
        if (CollectionUtil.isEmpty(values)) {
            return null;
        }
        return values.iterator().next();
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public HttpResponseWrapper wrap(Response response) {
        final String statusLine = String.valueOf(response.status());
        final List<StringTuple> headers = new ArrayList<>(response.headers().size());
        response.headers().forEach((k, v) -> headers.add(new StringTuple(k, v.iterator().next())));
        HttpResponseWrapper responseWrapper = new HttpResponseWrapper(statusLine, responseBody, null, headers);
        responseWrapper.setReason(response.reason());
        return responseWrapper;
    }

    @Override
    public Response unwrap(HttpResponseWrapper wrapped) {
        final int status = parseInt(wrapped.getStatusLine());
        byte[] responseContent = wrapped.getContent();
        final List<StringTuple> wrappedHeaders = wrapped.getHeaders();
        Map<String, Collection<String>> headers = new HashMap<>(wrappedHeaders.size());
        for (StringTuple header : wrappedHeaders) {
            headers.put(header.name(), Collections.singletonList(header.value()));
        }
        return Response.builder().body(responseContent).status(status).headers(headers).reason(wrapped.getReason()).request(request).build();
    }

    private int parseInt(String statusLine) {
        try {
            return Integer.parseInt(statusLine);
        } catch (Exception ex) {
            LogManager.warn("feign.parseInt", "statusLine: " + statusLine, ex);
            return -1;
        }
    }

    public Response copyResponse(Response response) {
        if (response == null) {
            return null;
        }
        final Body body = response.body();
        if (body == null) {
            return response;
        }
        try {
            responseBody = Util.toByteArray(body.asInputStream());
        } catch (Exception ex) {
            LogManager.warn("feign.copyResponse", "uri: " + getUri(), ex);
        }
        if (body.isRepeatable()) {
            return response;
        }
        return response.toBuilder().body(responseBody).build();
    }
}
