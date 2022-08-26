package io.arex.inst.httpservlet;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.listener.CaseEvent;
import io.arex.foundation.listener.CaseListenerImpl;
import io.arex.foundation.model.Constants;
import io.arex.foundation.model.ServiceEntranceMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.inst.httpservlet.adapter.ServletAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
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
        doExecute();
        executePostProcess();
    }

    private void executePostProcess() throws IOException {
        setResponseHeader();
        CaseListenerImpl.INSTANCE.onEvent(new CaseEvent(this, CaseEvent.Action.DESTROY));
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
        ServiceEntranceMocker mocker = new ServiceEntranceMocker();
        mocker.setMethod(adapter.getMethod(httpServletRequest));
        mocker.setPath(adapter.getServletPath(httpServletRequest));
        mocker.setPattern(getPattern());
        mocker.setRequestHeaders(SerializeUtils.serialize(getRequestHeaders()));
        mocker.setResponseHeaders(SerializeUtils.serialize(getResponseHeaders()));
        mocker.setRequest(getRequest());
        mocker.setResponse(getResponse());

        if (ContextManager.needReplay()) {
            mocker.replay();
        } else if (ContextManager.needRecord()) {
            mocker.record();
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