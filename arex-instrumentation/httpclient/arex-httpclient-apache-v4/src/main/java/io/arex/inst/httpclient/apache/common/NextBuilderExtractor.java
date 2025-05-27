package io.arex.inst.httpclient.apache.common;


import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.NextBuilderMock;
import io.arex.agent.bootstrap.model.NextBuilderMockDataQueryResponse;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.httpclient.common.HttpResponseWrapper.StringTuple;
import io.arex.inst.runtime.config.NextBuilderConfig;
import io.arex.inst.runtime.util.NextBuilderMockUtils;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NextBuilderExtractor
 *
 * @author ywqiu
 * @date 2025/4/23 21:35
 */
public class NextBuilderExtractor<TRequest, TResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NextBuilderExtractor.class);

    private final HttpClientAdapter<TRequest, TResponse> adapter;

    public NextBuilderExtractor(@Nonnull HttpClientAdapter<TRequest, TResponse> adapter) {
        this.adapter = adapter;
    }

    public MockResult mock() {
        boolean ignoreResult = true;
        if (CollectionUtil.isEmpty(NextBuilderConfig.get().getRequestList())
            || !NextBuilderConfig.get().getRequestList().contains(adapter.getUri().toString())) {
            return MockResult.success(true, null);
        }
        NextBuilderMockDataQueryResponse mockDataResponse = NextBuilderMockUtils.queryMock(makeMocker());
        if (mockDataResponse == null || StringUtil.isEmpty(mockDataResponse.getResponseContent())) {
            return MockResult.success(true, null);
        }
        HttpResponseWrapper responseWrapper = getHttpResponseWrapper(mockDataResponse);
        TResponse response = this.adapter.unwrap(responseWrapper);
        return MockResult.success(false, response);
    }

    private static HttpResponseWrapper getHttpResponseWrapper(NextBuilderMockDataQueryResponse mockDataResponse) {
        String mockResponse = mockDataResponse.getResponseContent();
        byte[] responseBody = mockResponse.getBytes(StandardCharsets.UTF_8);
        StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null);
        Locale locale = Locale.getDefault();

        return new HttpResponseWrapper(
            statusLine.toString(),
            responseBody,
            new StringTuple(locale.getLanguage(), locale.getCountry()),
            new ArrayList<StringTuple>());
    }

    private NextBuilderMock makeMocker() {
        String httpMethod = adapter.getMethod();
        NextBuilderMock mocker = NextBuilderMockUtils.createApacheHttpClientMock(adapter.getUri().toString());
        mocker.setOriginRequestBody(this.encodeRequest(httpMethod));
        mocker.setRequestMethod(httpMethod);
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

    private static final List<String> ALLOW_HTTP_METHOD_BODY_SETS;

    static {
        ALLOW_HTTP_METHOD_BODY_SETS = new ArrayList<>(4);
        ALLOW_HTTP_METHOD_BODY_SETS.add("POST");
        ALLOW_HTTP_METHOD_BODY_SETS.add("PUT");
        ALLOW_HTTP_METHOD_BODY_SETS.add("PATCH");
        ALLOW_HTTP_METHOD_BODY_SETS.add("DELETE");
    }
}
