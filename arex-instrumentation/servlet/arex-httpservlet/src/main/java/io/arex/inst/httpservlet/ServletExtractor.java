package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import java.io.IOException;
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
        if (adapter.getResponseHeader(httpServletResponse, ArexConstants.RECORD_ID) != null ||
                adapter.getResponseHeader(httpServletResponse, ArexConstants.REPLAY_ID) != null) {
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
            adapter.setResponseHeader(httpServletResponse, ArexConstants.RECORD_ID,
                    ContextManager.currentContext().getCaseId());
        }

        if (ContextManager.needReplay()) {
            adapter.setResponseHeader(httpServletResponse, ArexConstants.REPLAY_ID,
                    ContextManager.currentContext().getReplayId());
        }
    }

    private void doExecute() {
        Mocker mocker = MockUtils.createServlet(getPattern());

        Map<String, Object> requestAttributes = new HashMap<>();
        requestAttributes.put("HttpMethod", adapter.getMethod(httpServletRequest));
        requestAttributes.put("RequestPath", adapter.getServletPath(httpServletRequest));
        requestAttributes.put("Headers", getRequestHeaders());

        Map<String, Object> responseAttributes = Collections.singletonMap("Headers", getResponseHeaders());

        mocker.getTargetRequest().setAttributes(requestAttributes);
        mocker.getTargetRequest().setBody(getRequest());
        mocker.getTargetResponse().setAttributes(responseAttributes);

        Object response = getResponse();
        mocker.getTargetResponse().setBody(Serializer.serialize(response));
        mocker.getTargetResponse().setType(TypeUtil.getName(response));
        if (ContextManager.needReplay()) {
            MockUtils.replayMocker(mocker);
        } else if (ContextManager.needRecord()) {
            MockUtils.recordMocker(mocker);
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

    private Object getResponse() {
        Object response = adapter.getAttribute(httpServletRequest, ServletAdviceHelper.SERVLET_RESPONSE);
        // response body to json
        if (response != null) {
            adapter.removeAttribute(httpServletRequest, ServletAdviceHelper.SERVLET_RESPONSE);
            return response;
        }
        // view to html
        return adapter.getResponseBytes(httpServletResponse);
    }

    private String getPattern() {
        Object pattern = adapter
                .getAttribute(httpServletRequest, "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
        return pattern == null ? "" : String.valueOf(pattern);
    }
}