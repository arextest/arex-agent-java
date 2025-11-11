package io.arex.inst.httpclient.resttemplate;

import static io.arex.inst.runtime.model.ArexConstants.GSON_REQUEST_SERIALIZER;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.NextBuilderMockContext;
import io.arex.inst.runtime.config.NextBuilderConfig;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.service.ExtensionLogService;
import io.arex.inst.runtime.util.NextBuilderMockUtils;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RequestCallback;

/**
 * NextBuilderRestTemplateExtractor
 *
 * @author ywqiu
 * @date 2025/11/4 13:54
 */
public class NextBuilderRestTemplateExtractor {

    private final String LOGGER_TITLE = "NextBuilder-restTemplate";

    public static boolean openMock() {
        return NextBuilderConfig.get().isOpenMock();
    }

    public MockResult mock(URI uri, HttpMethod httpMethod, RequestCallback requestCallback) {
        try {
            String sourceUrl = uri.toString();
            if (NextBuilderConfig.get().inValidMockUrl(sourceUrl)) {
                ExtensionLogService.getInstance().info(LOGGER_TITLE,
                    "current uri dont need mock, currentUri:" + sourceUrl,
                    NextBuilderMockUtils.buildLogTag(sourceUrl));
                return MockResult.success(true, null);
            }
            NextBuilderMockContext nextBuilderMockContext = NextBuilderMockUtils.getNextBuilderMockContext(
                uri.toString(),
                httpMethod.name(),
                getBodyByReflection(requestCallback)
            );
            if (nextBuilderMockContext.getMockResponseBody() == null) {
                return MockResult.success(nextBuilderMockContext.isInterruptOriginalRequest(), null);
            }

            return MockResult.success(false, unWrap(nextBuilderMockContext));
        } catch (Throwable ex) {
            LogManager.warn(LOGGER_TITLE, ex);
            return null;
        }
    }

    private String getBodyByReflection(RequestCallback requestCallback) {
        try {
            Field entityField = requestCallback.getClass().getDeclaredField("requestEntity");
            entityField.setAccessible(true);
            if (entityField.get(requestCallback) instanceof HttpEntity) {
                HttpEntity<?> httpEntity = (HttpEntity<?>) entityField.get(requestCallback);
                if (httpEntity.getBody() != null) {
                    return Serializer.serialize(httpEntity.getBody(), GSON_REQUEST_SERIALIZER);
                }
            }
        } catch (Exception ex) {
            LogManager.warn(LOGGER_TITLE, ex);
        }
        return "";
    }

    private Object unWrap(NextBuilderMockContext nextBuilderMockContext) {

        ResponseEntityWrapper response = new ResponseEntityWrapper();
        response.setHttpStatus(HttpStatus.OK.value());
        response.setHeaders(new HashMap<>());
        if ("gzip".equalsIgnoreCase(nextBuilderMockContext.getAcceptEncoding())) {
            response.getHeaders().put(HttpHeaders.CONTENT_ENCODING, "gzip");
            response.setBody(NextBuilderMockUtils.compressString(nextBuilderMockContext.getMockResponseBody()));
        } else {
            response.setBody(nextBuilderMockContext.getMockResponseBody().getBytes(StandardCharsets.UTF_8));
        }
        return response.toResponseEntity();
    }
}
