package io.arex.inst.httpclient.common;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
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

    public void record(TResponse response) {
        HttpResponseWrapper wrapped = null;
        try {
            wrapped = adapter.wrap(response);
        } catch (Throwable throwable) {
            LOGGER.warn("wrap record error:{}", throwable.getMessage(), throwable);
        }

        if (wrapped == null) {
            return;
        }

        Mocker mocker = makeMocker();
        mocker.getTargetResponse().setType(HttpResponseWrapper.class.getName());
        mocker.getTargetResponse().setBody(Serializer.serialize(wrapped));
        MockUtils.recordMocker(mocker);
    }

    public void record(Throwable throwable) {
        Mocker mocker = makeMocker();
        mocker.getTargetResponse().setType(TypeUtil.getName(throwable));
        mocker.getTargetResponse().setBody(Serializer.serialize(throwable));
        MockUtils.recordMocker(mocker);
    }

    public MockResult replay() {
        boolean ignoreResult = IgnoreUtils.ignoreMockResult("http", adapter.getUri().getPath());
        Object object = MockUtils.replayBody(makeMocker());
        if (object instanceof Throwable) {
            return MockResult.success(ignoreResult, object);
        }
        if (object instanceof HttpResponseWrapper) {
            TResponse response = this.adapter.unwrap((HttpResponseWrapper) object);
            return MockResult.success(ignoreResult, response);
        }
        return null;
    }

    private Mocker makeMocker() {
        String httpMethod = adapter.getMethod();
        Mocker mocker = MockUtils.createHttpClient(adapter.getUri().getPath());
        Map<String, Object> attributes = new HashMap<>();

        mocker.getTargetRequest().setAttributes(attributes);
        attributes.put("HttpMethod", httpMethod);
        attributes.put("QueryString", adapter.getUri().getQuery());
        attributes.put("ContentType", adapter.getRequestContentType());

        mocker.getTargetRequest().setBody(this.encodeRequest(httpMethod));
        return mocker;
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