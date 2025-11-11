package io.arex.inst.httpclient.apache.common;


import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.NextBuilderMock;
import io.arex.agent.bootstrap.model.NextBuilderMockContext;
import io.arex.agent.bootstrap.model.NextBuilderMockDataQueryResponse;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.httpclient.common.HttpResponseWrapper.StringTuple;
import io.arex.inst.runtime.config.NextBuilderConfig;
import io.arex.inst.runtime.service.ExtensionLogService;
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

/**
 * NextBuilderExtractor
 *
 * @author ywqiu
 * @date 2025/4/23 21:35
 */
public class NextBuilderExtractor<TRequest, TResponse> {

    private static final String LOGGER_TITLE = "NextBuilder-apacheHttpclient";
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
        String sourceUrl = adapter.getUri().toString();
        if (NextBuilderConfig.get().inValidMockUrl(sourceUrl)) {
            ExtensionLogService.getInstance().info(LOGGER_TITLE,
                "current uri dont need mock, currentUri:" + sourceUrl,
                NextBuilderMockUtils.buildLogTag(sourceUrl));
            return MockResult.success(true, null);
        }
        NextBuilderMockContext nextBuilderMockContext = NextBuilderMockUtils.getNextBuilderMockContext(sourceUrl,
            adapter.getMethod(),
            this.getRequestBody(adapter.getMethod()));

        if (nextBuilderMockContext.getMockResponseBody() == null) {
            return MockResult.success(nextBuilderMockContext.isInterruptOriginalRequest(), null);
        }
        HttpResponseWrapper responseWrapper = getHttpResponseWrapper(nextBuilderMockContext);
        TResponse response = this.adapter.unwrap(responseWrapper);
        return MockResult.success(false, response);
    }

    private static HttpResponseWrapper getHttpResponseWrapper(NextBuilderMockContext nextBuilderMockContext) {
        String mockResponse = nextBuilderMockContext.getMockResponseBody();
        byte[] responseBody = mockResponse.getBytes(StandardCharsets.UTF_8);
        StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null);
        Locale locale = Locale.getDefault();

        return new HttpResponseWrapper(
            statusLine.toString(),
            responseBody,
            new StringTuple(locale.getLanguage(), locale.getCountry()),
            new ArrayList<StringTuple>());
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
