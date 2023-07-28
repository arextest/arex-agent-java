package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.converter.HttpMessageConvertFactory;
import io.arex.inst.httpservlet.converter.HttpMessageConverter;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
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
    private static final int HTTP_STATUS_OK = 200;
    private static final int HTTP_STATUS_FOUND = 302;
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
        // Response status is 302, record redirect request
        if (HTTP_STATUS_FOUND == adapter.getStatus(httpServletResponse)) {
            ArexContext context = ContextManager.currentContext();
            context.setAttachment(ArexConstants.REDIRECT_REQUEST_METHOD, adapter.getMethod(httpServletRequest));
            context.setAttachment(ArexConstants.REDIRECT_REFERER, adapter.getFullUrl(httpServletRequest));
            context.setAttachment(ArexConstants.REDIRECT_PATTERN, adapter.getPattern(httpServletRequest));
            adapter.copyBodyToResponse(httpServletResponse);
            return;
        }

        // Do not record if response status is not OK
        if (HTTP_STATUS_OK != adapter.getStatus(httpServletResponse)) {
            adapter.copyBodyToResponse(httpServletResponse);
            return;
        }

        if (adapter.getResponseHeader(httpServletResponse, ArexConstants.RECORD_ID) != null ||
                adapter.getResponseHeader(httpServletResponse, ArexConstants.REPLAY_ID) != null) {
            adapter.copyBodyToResponse(httpServletResponse);
            return;
        }

        if (!ContextManager.needRecordOrReplay()) {
            adapter.copyBodyToResponse(httpServletResponse);
            return;
        }

        setResponseHeader();
        doExecute();
        adapter.removeAttribute(httpServletRequest, ServletAdviceHelper.SERVLET_ASYNC_FLAG);
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
        String pattern;
        String httpMethod;
        String requestPath;
        ArexContext context = ContextManager.currentContext();
        if (context.isRedirectRequest()) {
            pattern = String.valueOf(context.getAttachment(ArexConstants.REDIRECT_PATTERN));
            httpMethod = String.valueOf(context.getAttachment(ArexConstants.REDIRECT_REQUEST_METHOD));
            requestPath = ServletUtil.getRequestPath(adapter.getRequestHeader(httpServletRequest, "referer"));
        } else {
            pattern = adapter.getPattern(httpServletRequest);
            httpMethod = adapter.getMethod(httpServletRequest);
            requestPath = adapter.getRequestPath(httpServletRequest);
        }

        Map<String, Object> requestAttributes = getRequestAttributes();
        requestAttributes.put("HttpMethod", httpMethod);
        requestAttributes.put("RequestPath", requestPath);
        Map<String,String> requestHeaders = getRequestHeaders();
        requestAttributes.put("Headers", requestHeaders);
        requestAttributes.put(ArexConstants.CONFIG_VERSION,
                adapter.getAttribute(httpServletRequest, ArexConstants.CONFIG_VERSION));

        String originalMocker = requestHeaders.get(ArexConstants.REPLAY_ORIGINAL_MOCKER);
        MockCategoryType mockCategoryType =
            originalMocker == null ? MockCategoryType.SERVLET : MockCategoryType.createEntryPoint(originalMocker);
        Mocker mocker = MockUtils.create(mockCategoryType, pattern);

        mocker.getTargetRequest().setAttributes(requestAttributes);
        mocker.getTargetRequest().setBody(getRequest());
        mocker.getTargetResponse().setAttributes(Collections.singletonMap("Headers", getResponseHeaders()));

        Object response = getResponse();
        String responseString = response instanceof String ? (String) response : Serializer.serialize(response);
        mocker.getTargetResponse().setBody(responseString);
        mocker.getTargetResponse().setType(TypeUtil.getName(response));
        if (ContextManager.needReplay()) {
            MockUtils.replayMocker(mocker);
        } else if (ContextManager.needRecord()) {
            MockUtils.recordMocker(mocker);
        }
    }

    private Map<String, Object> getRequestAttributes() {
        try {
            final Object extensionAttr = adapter.getAttribute(httpServletRequest, ArexConstants.AREX_EXTENSION_ATTRIBUTE);
            if (extensionAttr instanceof Map) {
                return (Map<String, Object>) extensionAttr;
            }
        } catch (Throwable ex) {
            LogManager.warn("getRequestAttr", ex);
        }
        return new HashMap<>();
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        final Enumeration<String> headerNames = adapter.getRequestHeaderNames(httpServletRequest);
        while (headerNames.hasMoreElements()) {
            final String key = headerNames.nextElement();
            // ignore referer
            if ("referer".equals(key)) {
                continue;
            }
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
        HttpMessageConverter converter = HttpMessageConvertFactory.getSupportedConverter(
            httpServletRequest, adapter);
        return Base64.getEncoder().encodeToString(converter.getRequest(httpServletRequest, adapter));
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

}
