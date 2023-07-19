package io.arex.foundation.util.httpclient;

import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.foundation.model.HttpClientResponse;
import io.arex.inst.runtime.log.LogManager;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.slf4j.MDC;

public class HttpClientResponseCallback implements FutureCallback<HttpResponse> {

    private final Map<String, String> contextMap;
    private final HttpClientResponseHandler responseHandler;
    private final CompletableFuture<HttpClientResponse> responseFuture;

    public HttpClientResponseCallback(CompletableFuture<HttpClientResponse> responseFuture,
        HttpClientResponseHandler responseHandler) {
        this.contextMap = MDC.getCopyOfContextMap();
        this.responseFuture = responseFuture;
        this.responseHandler = responseHandler;
    }

    @Override
    public void completed(HttpResponse response) {
        LogManager.setContextMap(contextMap);
        HttpClientResponse clientResponse = new HttpClientResponse();

        if (response.getStatusLine() != null) {
            clientResponse.setStatusCode(response.getStatusLine().getStatusCode());
        }

        clientResponse.setHeaders(convertResponseHeaders(response.getAllHeaders()));

        if (HttpStatus.SC_OK == clientResponse.getStatusCode()) {
            try {
                clientResponse.setBody(responseHandler.handle(response.getEntity()));
            } catch (Exception e) {
                LogManager.warn("completed", e);
            }
        }

        responseFuture.complete(clientResponse);
        close(response);
    }

    @Override
    public void failed(Exception e) {
        LogManager.setContextMap(contextMap);
        responseFuture.completeExceptionally(e);
        LogManager.warn("failed", e);
    }

    @Override
    public void cancelled() {
        LogManager.setContextMap(contextMap);
        responseFuture.completeExceptionally(new InterruptedException("Request has been cancelled."));
        LogManager.warn("cancelled", "Request has been cancelled.");
    }

    private Map<String, String> convertResponseHeaders(Header[] headers) {
        if (ArrayUtils.isEmpty(headers)) {
            return Collections.emptyMap();
        }
        Map<String, String> responseHeaders = MapUtils.newHashMapWithExpectedSize(headers.length);
        for (Header header : headers) {
            responseHeaders.put(header.getName(), header.getValue());
        }
        return responseHeaders;
    }

    private static void close(HttpResponse response) {
        if (response == null) {
            return;
        }

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            EntityUtils.consumeQuietly(entity);
        }
    }
}
