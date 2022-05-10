package io.arex.inst.httpservlet;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.internal.Pair;
import io.arex.foundation.util.LogUtil;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.method.support.InvocableHandlerMethod;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * ServletAdviceHelper
 */
public class ServletAdviceHelper {
    public static <HttpServletRequest, HttpServletResponse> Pair<HttpServletRequest, HttpServletResponse> onServiceEnter(
        ServletAdapter<HttpServletRequest, HttpServletResponse> adapter, Object servletRequest,
        Object servletResponse) {
        HttpServletRequest httpServletRequest = adapter.asHttpServletRequest(servletRequest);
        HttpServletResponse httpServletResponse = adapter.asHttpServletResponse(servletResponse);

        // Do nothing if request header with arex-replay-warm-up
        if (Boolean.parseBoolean(adapter.getRequestHeader(httpServletRequest, ServletConstants.REPLAY_WARM_UP))) {
            return null;
        }

        // Async listener will handle if attr with arex-async-flag
        if ("true".equals(adapter.getAttribute(httpServletRequest, ServletConstants.SERVLET_ASYNC_FLAG))) {
            httpServletResponse = adapter.wrapResponse(httpServletResponse);
            return Pair.of(null, httpServletResponse);
        }

        String caseId = adapter.getRequestHeader(httpServletRequest, ServletConstants.RECORD_ID);
        ContextManager.currentContext(true, caseId);
        if (ContextManager.needRecordOrReplay()) {
            httpServletRequest = adapter.wrapRequest(httpServletRequest);
            httpServletResponse = adapter.wrapResponse(httpServletResponse);
            return Pair.of(httpServletRequest, httpServletResponse);
        }

        return null;
    }


    public static <HttpServletRequest, HttpServletResponse> void onServiceExit(
        ServletAdapter<HttpServletRequest, HttpServletResponse> adapter, Object servletRequest,
        Object servletResponse) {
        try {
            if (!ContextManager.needRecordOrReplay()) {
                return;
            }

            HttpServletRequest httpServletRequest = adapter.asHttpServletRequest(servletRequest);
            HttpServletResponse httpServletResponse = adapter.asHttpServletResponse(servletResponse);

            if (!adapter.wrapped(httpServletRequest, httpServletResponse)) {
                return;
            }

            if (HttpStatus.SC_OK != adapter.getStatus(httpServletResponse)) {
                adapter.copyBodyToResponse(httpServletResponse);
                return;
            }

            // Async listener will handle async request
            if ("true".equals(adapter.getAttribute(httpServletRequest, ServletConstants.SERVLET_ASYNC_FLAG))) {
                adapter.removeAttribute(httpServletRequest, ServletConstants.SERVLET_ASYNC_FLAG);
                adapter.copyBodyToResponse(httpServletResponse);
                return;
            }

            // Add async listener for async request
            if (adapter.isAsyncStarted(httpServletRequest)) {
                adapter.setAttribute(httpServletRequest, ServletConstants.SERVLET_ASYNC_FLAG, "true");
                adapter.addListener(adapter, httpServletRequest, httpServletResponse);
                return;
            }

            // sync request
            new ServletExtractor<>(adapter, httpServletRequest, httpServletResponse).execute();
        } catch (Throwable e) {
            LogUtil.warn("servlet.onExit", e);
        }
    }

    public static <HttpServletRequest, HttpServletResponse> void onInvokeForRequestExit(
        ServletAdapter<HttpServletRequest, HttpServletResponse> adapter, NativeWebRequest nativeWebRequest,
        InvocableHandlerMethod invocableHandlerMethod, Object response) {
        if (response == null) {
            return;
        }

        if (!ContextManager.needRecordOrReplay()) {
            return;
        }

        // Do not set when async request
        if (response instanceof CompletableFuture || response instanceof DeferredResult
            || response instanceof Callable) {
            return;
        }

        // Set response only when return response body
        if (!invocableHandlerMethod.getReturnType().hasMethodAnnotation(ResponseBody.class)) {
            return;
        }

        HttpServletRequest httpServletRequest = adapter.getNativeRequest(nativeWebRequest);

        if (httpServletRequest == null) {
            return;
        }

        adapter.setAttribute(httpServletRequest, ServletConstants.SERVLET_RESPONSE, response);
    }
}
