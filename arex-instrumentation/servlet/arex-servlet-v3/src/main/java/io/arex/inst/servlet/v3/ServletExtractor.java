package io.arex.inst.servlet.v3;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.model.ServletMocker;
import io.arex.foundation.serializer.SerializeUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * ServletExtractor
 *
 *
 * @date 2022/03/03
 */
public class ServletExtractor {
    private final CachedBodyRequestWrapper requestWrapper;
    private final CachedBodyResponseWrapper responseWrapper;

    public ServletExtractor(CachedBodyRequestWrapper requestWrapper,
        CachedBodyResponseWrapper responseWrapper) {
        this.requestWrapper = requestWrapper;
        this.responseWrapper = responseWrapper;
    }

    public void execute() {
        ServletMocker mocker = new ServletMocker();
        mocker.setMethod(requestWrapper.getMethod());
        mocker.setPath(requestWrapper.getServletPath());
        mocker.setPattern(getPattern());
        mocker.setRequestHeaders(getRequestHeaders());
        mocker.setResponseHeaders(getResponseHeaders());
        mocker.setRequest(getRequest());
        mocker.setResponse(getResponse());

        if(ContextManager.needReplay()) {
            mocker.replay();
        } else if (ContextManager.needRecord()) {
            mocker.record();
        }
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        final Enumeration<String> headerNames = requestWrapper.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String key = headerNames.nextElement();
            headers.put(key, requestWrapper.getHeader(key));
        }
        return headers;
    }

    private Map<String, String> getResponseHeaders() {
        Map<String, String> headers = new HashMap<>();
        final Collection<String> headerNames = responseWrapper.getHeaderNames();
        for (String key : headerNames) {
            headers.put(key, responseWrapper.getHeader(key));
        }
        return headers;
    }

    private String getRequest() {
        if ("GET".equals(requestWrapper.getMethod())) {
            return requestWrapper.getQueryString();
        }
        // Compatible with custom message converters that include compression
        return Base64.getEncoder().encodeToString(requestWrapper.getContentAsByteArray());
    }

    private String getResponse() {
        Object response = ServletUtils.getServletResponse(requestWrapper);
        // response body to json
        if (response != null) {
            return SerializeUtils.serialize(response);
        }
        // view to html
        return new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    private String getPattern() {
        Object pattern = requestWrapper.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
        return pattern == null ? "" : String.valueOf(pattern);
    }
}
