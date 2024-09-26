package io.arex.inst.httpclient.asynchttpclient;

import static io.arex.inst.httpclient.common.HttpClientExtractor.ALLOW_HTTP_METHOD_BODY_SETS;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.httpclient.asynchttpclient.wrapper.ResponseWrapper;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.asynchttpclient.Request;

public class AsyncHttpClientExtractor {
    private static final byte[] ZERO_BYTE = new byte[0];
    private final Request request;
    private final ResponseWrapper response;

    public AsyncHttpClientExtractor(Request request, ResponseWrapper responseWrapper) {
        this.request = request;
        this.response = responseWrapper;
    }


    public void record() {
        Mocker mocker = makeMocker();
        mocker.getTargetResponse().setType(TypeUtil.getName(response));
        mocker.getTargetResponse().setBody(Serializer.serialize(response));
        MockUtils.recordMocker(mocker);
    }

    protected Mocker makeMocker() {
        String httpMethod = request.getMethod();
        Mocker mocker = MockUtils.createHttpClient(request.getUri().getPath());
        Map<String, Object> attributes = new HashMap<>(3);

        mocker.getTargetRequest().setAttributes(attributes);
        attributes.put("HttpMethod", httpMethod);
        attributes.put("QueryString", request.getUri().getQuery());
        attributes.put("ContentType", request.getHeaders().get("Content-Type"));

        mocker.getTargetRequest().setBody(encodeRequest(httpMethod));
        return mocker;
    }

    protected String encodeRequest(String httpMethod) {
        if (ALLOW_HTTP_METHOD_BODY_SETS.contains(httpMethod)) {
            byte[] bytes = getRequestBytes();
            if (bytes != null) {
                return Base64.getEncoder().encodeToString(bytes);
            }
        }
        return request.getUri().getQuery();
    }

    private byte[] getRequestBytes() {
        if (request.getByteData() != null) {
            return request.getByteData();
        }

        if (request.getCompositeByteData() != null) {
            return request.getCompositeByteData().toString().getBytes(StandardCharsets.UTF_8);
        }

        if (request.getStringData() != null) {
            return request.getStringData().getBytes(StandardCharsets.UTF_8);
        }

        if (request.getByteBufferData() != null) {
            return request.getByteBufferData().array();

        }

        return ZERO_BYTE;
    }


    public MockResult replay() {
        final boolean ignoreMockResult = IgnoreUtils.ignoreMockResult("http", request.getUri().getPath());
        final Object replayBody = MockUtils.replayBody(makeMocker());
        return MockResult.success(ignoreMockResult, replayBody);
    }

    public void record(Throwable throwable) {
        Mocker mocker = makeMocker();
        mocker.getTargetResponse().setType(TypeUtil.getName(throwable));
        mocker.getTargetResponse().setBody(Serializer.serialize(throwable));
        MockUtils.recordMocker(mocker);
    }
}
