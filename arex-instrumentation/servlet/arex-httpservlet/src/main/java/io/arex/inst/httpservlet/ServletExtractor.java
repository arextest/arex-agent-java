package io.arex.inst.httpservlet;

import com.arextest.model.constants.MockAttributeNames;
import com.arextest.model.mock.AREXMocker;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.model.Constants;
import io.arex.foundation.model.MockerUtils;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.inst.httpservlet.adapter.ServletAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * ServletWrapper
 *
 * @date 2022/03/03
 */
public class ServletExtractor<HttpServletRequest, HttpServletResponse> {
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;
    private final ServletAdapter<HttpServletRequest, HttpServletResponse> adapter;

    public ServletExtractor(ServletAdapter<HttpServletRequest, HttpServletResponse> adapter,
                            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
        this.adapter = adapter;
    }

    public void execute() throws IOException {
        if (adapter.getResponseHeader(httpServletResponse, Constants.RECORD_ID) != null ||
                adapter.getResponseHeader(httpServletResponse, Constants.REPLAY_ID) != null) {
            adapter.copyBodyToResponse(httpServletResponse);
            return;
        }

        if (!ContextManager.needRecordOrReplay()) {
            return;
        }

        doExecute();
        executePostProcess();
    }

    private void executePostProcess() throws IOException {
        setResponseHeader();
        adapter.copyBodyToResponse(httpServletResponse);
    }


    private void setResponseHeader() {
        if (ContextManager.needRecord()) {
            adapter.setResponseHeader(httpServletResponse, Constants.RECORD_ID,
                    ContextManager.currentContext().getCaseId());
        }

        if (ContextManager.needReplay()) {
            adapter.setResponseHeader(httpServletResponse, Constants.REPLAY_ID,
                    ContextManager.currentContext().getReplayId());
        }
    }

    private void doExecute() {
        AREXMocker mocker = MockerUtils.createServlet(getPattern());
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MockAttributeNames.HTTP_METHOD, adapter.getMethod(httpServletRequest));
        attributes.put(MockAttributeNames.SERVLET_PATH, adapter.getServletPath(httpServletRequest));
        attributes.put(MockAttributeNames.HEADERS, getRequestHeaders());
        mocker.getTargetRequest().setAttributes(attributes);
        mocker.getTargetRequest().setBody(getRequest());
        attributes = Collections.singletonMap(MockAttributeNames.HEADERS, getResponseHeaders());
        mocker.getTargetResponse().setAttributes(attributes);
        mocker.getTargetResponse().setBody(getResponse());
        if (ContextManager.needReplay()) {
            MockerUtils.replayBody(mocker);
        } else if (ContextManager.needRecord()) {
            MockerUtils.record(mocker);
        }
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        final Enumeration<String> headerNames = adapter.getRequestHeaderNames(httpServletRequest);
        while (headerNames.hasMoreElements()) {
            final String key = headerNames.nextElement();
            headers.put(key, adapter.getRequestHeader(httpServletRequest, key));
        }
        return headers;
    }

    private Map<String, String> getResponseHeaders() {
        Map<String, String> headers = new HashMap<>();
        final Collection<String> headerNames = adapter.getResponseHeaderNames(httpServletResponse);
        for (String key : headerNames) {
            headers.put(key, adapter.getResponseHeader(httpServletResponse, key));
        }
        return headers;
    }

    private String getRequest() {
        if ("GET".equals(adapter.getMethod(httpServletRequest))) {
            return adapter.getQueryString(httpServletRequest);
        }
        // Compatible with custom message converters that include compression
        return Base64.getEncoder().encodeToString(adapter.getRequestBytes(httpServletRequest));
    }

    private String getResponse() {
        Object response = adapter.getAttribute(httpServletRequest, ServletConstants.SERVLET_RESPONSE);
        // response body to json
        if (response != null) {
            adapter.removeAttribute(httpServletRequest, ServletConstants.SERVLET_RESPONSE);
            return SerializeUtils.serialize(response);
        }
        // view to html
        return new String(adapter.getResponseBytes(httpServletResponse), StandardCharsets.UTF_8);
    }

    private String getPattern() {
        Object pattern = adapter
                .getAttribute(httpServletRequest, "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
        return pattern == null ? "" : String.valueOf(pattern);
    }
}