package io.arex.inst.httpclient.common;

import com.arextest.model.constants.MockAttributeNames;
import com.arextest.model.mock.AREXMocker;
import com.arextest.model.mock.Mocker;
import io.arex.foundation.model.MockerUtils;
import io.arex.foundation.serializer.SerializeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Mocker mocker = this.makeMocker(response);
            MockerUtils.record(mocker);
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

    private Mocker makeMocker(String response) {
        String httpMethod = adapter.getMethod();
        AREXMocker mocker = MockerUtils.createHttpClient(httpMethod);
        Map<String, Object> attributes = new HashMap<>();
        mocker.getTargetRequest().setAttributes(attributes);
        attributes.put(MockAttributeNames.URL, adapter.getUri().toString());
        attributes.put(MockAttributeNames.CONTENT_TYPE, adapter.getRequestContentType());
        mocker.getTargetRequest().setBody(this.encodeRequest(httpMethod));
        mocker.getTargetResponse().setType(HttpResponseWrapper.class.getName());
        mocker.getTargetResponse().setBody(response);
        return mocker;
    }

    public HttpResponseWrapper fetchMockResult() {
        Mocker mocker = makeMocker(StringUtils.EMPTY);
        HttpResponseWrapper wrapper = (HttpResponseWrapper) mocker.replay();
        wrapper.setIgnoreMockResult(mocker.ignoreMockResult());
        return wrapper;
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

    private final static List<String> ALLOW_HTTP_METHOD_BODY_SETS;

    static {
        ALLOW_HTTP_METHOD_BODY_SETS = new ArrayList<>(4);
        ALLOW_HTTP_METHOD_BODY_SETS.add("POST");
        ALLOW_HTTP_METHOD_BODY_SETS.add("PUT");
        ALLOW_HTTP_METHOD_BODY_SETS.add("PATCH");
        ALLOW_HTTP_METHOD_BODY_SETS.add("DELETE");
    }
}