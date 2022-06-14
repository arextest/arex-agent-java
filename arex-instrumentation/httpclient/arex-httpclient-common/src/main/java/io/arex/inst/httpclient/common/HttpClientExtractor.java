package io.arex.inst.httpclient.common;

import io.arex.foundation.model.HttpClientMocker;
import io.arex.foundation.serializer.SerializeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class HttpClientExtractor<TRequest, TResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientExtractor.class);

    private final HttpClientAdapter<TRequest, TResponse> adapter;

    public HttpClientExtractor(@Nonnull HttpClientAdapter<TRequest, TResponse> adapter) {
        this.adapter = adapter;
    }

    public void record(HttpResponseWrapper wrapped) {
        if (wrapped == null) {
            return;
        }
        try {
            String response = SerializeUtils.serialize(wrapped);
            HttpClientMocker mocker = this.makeMocker();
            mocker.setResponse(response);
            mocker.record();
        } catch (Throwable throwable) {
            LOGGER.warn("record error:{}", throwable.getMessage(), throwable);
        }
    }

    public void record(TResponse response) {
        HttpResponseWrapper wrapped = null;
        try {
            wrapped = adapter.wrap(response);
        } catch (Throwable throwable) {
            LOGGER.warn("wrap record error:{}", throwable.getMessage(), throwable);
        }
        this.record(wrapped);
    }

    public void record(Exception exception) {
        HttpResponseWrapper wrapped = HttpResponseWrapper.of(new ExceptionWrapper(exception));
        this.record(wrapped);
    }

    public TResponse replay() {
        return this.replay(fetchMockResult());
    }

    public TResponse replay(HttpResponseWrapper wrapped) {
        if (wrapped == null) {
            throw new ArexDataException("The wrapped == null");
        }
        ExceptionWrapper exception = wrapped.getException();
        if (exception != null) {
            throw new ArexDataException(exception.getOriginalException());
        }
        return this.adapter.unwrap(wrapped);
    }

    private HttpClientMocker makeMocker() {
        HttpClientMocker mocker = new HttpClientMocker();
        String httpMethod = adapter.getMethod();
        mocker.setMethod(httpMethod);
        mocker.setUrl(adapter.getUri().toString());
        mocker.setContentType(adapter.getRequestContentType());
        mocker.setRequest(this.encodeRequest(httpMethod));
        mocker.setResponseType(HttpResponseWrapper.class.getName());
        return mocker;
    }

    public HttpResponseWrapper fetchMockResult() {
        HttpClientMocker mocker = makeMocker();
        return (HttpResponseWrapper) mocker.replay();
    }

    private String encodeRequest(String httpMethod) {
        if (ALLOW_HTTP_METHOD_BODY_SETS.contains(httpMethod)) {
            byte[] bytes = adapter.getRequestBytes();
            if (bytes != null) {
                return Base64.getEncoder().encodeToString(bytes);
            }
        }
        return adapter.getUri().getQuery();
    }

    private final static Set<String> ALLOW_HTTP_METHOD_BODY_SETS;

    static {
        ALLOW_HTTP_METHOD_BODY_SETS = new HashSet<>();
        ALLOW_HTTP_METHOD_BODY_SETS.add("POST");
        ALLOW_HTTP_METHOD_BODY_SETS.add("PUT");
        ALLOW_HTTP_METHOD_BODY_SETS.add("PATCH");
        ALLOW_HTTP_METHOD_BODY_SETS.add("DELETE");
    }
}