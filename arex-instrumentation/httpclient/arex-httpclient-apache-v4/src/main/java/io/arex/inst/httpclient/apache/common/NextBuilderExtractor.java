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
import io.arex.inst.runtime.util.ZstdService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
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

    private final HttpClientAdapter<TRequest, TResponse> adapter;

    private static final List<String> ALLOW_HTTP_METHOD_BODY_SETS;

    static {
        ALLOW_HTTP_METHOD_BODY_SETS = new ArrayList<>(4);
        ALLOW_HTTP_METHOD_BODY_SETS.add("POST");
        ALLOW_HTTP_METHOD_BODY_SETS.add("PUT");
        ALLOW_HTTP_METHOD_BODY_SETS.add("PATCH");
        ALLOW_HTTP_METHOD_BODY_SETS.add("DELETE");
    }

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
        if (mockDataResponse == null
            || mockDataResponse.getResult() == null
            || StringUtil.isEmpty(mockDataResponse.getResult().getData())) {
            return MockResult.success(true, null);
        }
        HttpResponseWrapper responseWrapper = getHttpResponseWrapper(mockDataResponse);
        TResponse response = this.adapter.unwrap(responseWrapper);
        return MockResult.success(false, response);
    }

    private static HttpResponseWrapper getHttpResponseWrapper(NextBuilderMockDataQueryResponse mockDataResponse) {
        String mockResponse = mockDataResponse.getResult().getData();
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
        mocker.setOriginRequestBody(ZstdService.getInstance().serialize(this.getRequestBody(httpMethod)));
        mocker.setRequestMethod(httpMethod);
        return mocker;
    }

    private String getRequestBody(String httpMethod) {
        if (ALLOW_HTTP_METHOD_BODY_SETS.contains(httpMethod)) {
            byte[] bytes = adapter.getRequestBytes();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            try {
                GZIPInputStream gzipin = new GZIPInputStream(in);
                byte[] buffer = new byte[1024];
                int offset = -1;
                while ((offset = gzipin.read(buffer)) != -1) {
                    out.write(buffer, 0, offset);
                }
                return out.toString();
            } catch (Exception ex) {
                // 如果抛出异常，则说明不是gzip压缩，直接按原始内容返回
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
        return adapter.getUri().getQuery();
    }
}
