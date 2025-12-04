package io.arex.inst.httpclient.resttemplate;

import static io.arex.inst.runtime.model.ArexConstants.GSON_REQUEST_SERIALIZER;
import static io.arex.inst.runtime.model.ArexConstants.GSON_SERIALIZER;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;

public class RestTemplateExtractor {
    private static final String RESPONSE_ENTITY_WRAPPER_CLASS_NAME = ResponseEntityWrapper.class.getName();
    private final URI uri;
    private final HttpMethod httpMethod;
    private final String request;

    public RestTemplateExtractor(URI uri, HttpMethod httpMethod, RequestCallback requestCallback) {
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.request = Serializer.serialize(requestCallback, GSON_REQUEST_SERIALIZER);
    }

    public void record(Object response, Throwable throwable) {
        try {
            if (isExclude()) {
                return;
            }
            Mocker mocker = makeMocker();
            if (throwable != null) {
                mocker.getTargetResponse().setType(TypeUtil.getName(throwable));
                mocker.getTargetResponse().setBody(Serializer.serialize(throwable, GSON_SERIALIZER));
            } else {
                Object responseWrapper = wrapResponse(response);
                mocker.getTargetResponse().setType(getName(responseWrapper));
                mocker.getTargetResponse().setBody(Serializer.serialize(responseWrapper, GSON_SERIALIZER));
            }
            MockUtils.recordMocker(mocker);
        } catch (Throwable ex) {
            LogManager.warn("restTemplateRecord", ex);
        }
    }

    private String getName(Object response) {
        if (!(response instanceof ResponseEntityWrapper)) {
            return TypeUtil.getName(response);
        }
        ResponseEntityWrapper<?> responseEntityWrapper = (ResponseEntityWrapper<?>) response;

        if (responseEntityWrapper.getBody() == null) {
            return RESPONSE_ENTITY_WRAPPER_CLASS_NAME;
        }
        return RESPONSE_ENTITY_WRAPPER_CLASS_NAME + "-" + TypeUtil.getName(responseEntityWrapper.getBody());
    }

    private Object wrapResponse(Object response) {
        if (response instanceof ResponseEntity) {
            return new ResponseEntityWrapper((ResponseEntity) response);
        }
        return response;

    }

    private boolean isExclude() {
        return IgnoreUtils.excludeOperation(uri.getPath());
    }

    public MockResult replay() {
        try {
            if (isExclude()) {
                return MockResult.IGNORE_MOCK_RESULT;
            }
            boolean ignoreResult = IgnoreUtils.ignoreMockResult("http", uri.getPath());
            Mocker mocker = makeMocker();
            Mocker responseMocker = MockUtils.replayMocker(mocker);
            if (!MockUtils.checkResponseMocker(responseMocker)) {
                return null;
            }
            Object response = unWrap(responseMocker);
            return MockResult.success(ignoreResult, response);
        } catch (Throwable ex) {
            LogManager.warn("restTemplateReplay", ex);
            return null;
        }
    }

    private Object unWrap(Mocker responseMocker) {
        Object response = Serializer.deserialize(responseMocker.getTargetResponse().getBody(),
                TypeUtil.forName(responseMocker.getTargetResponse().getType()), GSON_SERIALIZER);
        if (response instanceof ResponseEntityWrapper) {
            return ((ResponseEntityWrapper) response).toResponseEntity();
        }
        return response;
    }

    private Mocker makeMocker() {
        ArexMocker mocker = MockUtils.createHttpClient(uri.getPath());
        Map<String, Object> attributes = new HashMap<>(2);

        mocker.getTargetRequest().setAttributes(attributes);
        attributes.put(ArexConstants.HTTP_METHOD, Objects.isNull(httpMethod) ? null : httpMethod.name());
        attributes.put(ArexConstants.HTTP_QUERY_STRING, uri.getQuery());

        mocker.getTargetRequest().setBody(request);
        return mocker;
    }
}
