package io.arex.inst.httpservlet;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.internal.Pair;
import io.arex.foundation.listener.CaseEvent;
import io.arex.foundation.listener.CaseListenerImpl;
import io.arex.foundation.util.LogUtil;
import io.arex.foundation.util.StringUtil;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.method.support.InvocableHandlerMethod;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * ServletAdviceHelper
 */
public class ServletAdviceHelper {
    private static final Set<String> FILTERED_CONTENT_TYPE = new HashSet<>();

    static {
        FILTERED_CONTENT_TYPE.add("/javascript");
        FILTERED_CONTENT_TYPE.add("image/");
        FILTERED_CONTENT_TYPE.add("/font");
        FILTERED_CONTENT_TYPE.add("/pdf");
        FILTERED_CONTENT_TYPE.add(".css");
    }

    public static <TRequest, TResponse> Pair<TRequest, TResponse> onServiceEnter(
            ServletAdapter<TRequest, TResponse> adapter, Object servletRequest,
            Object servletResponse) {
        TRequest httpServletRequest = adapter.asHttpServletRequest(servletRequest);
        if (shouldSkip(adapter, httpServletRequest)) {
            return null;
        }

        String caseId = adapter.getRequestHeader(httpServletRequest, ServletConstants.RECORD_ID);
        // check record rate limit
        if (StringUtil.isEmpty(caseId) && !checkRateLimit(adapter.getServletPath(httpServletRequest))) {
            return null;
        }
        TResponse httpServletResponse = adapter.asHttpServletResponse(servletResponse);

        // Async listener will handle if attr with arex-async-flag
        if ("true".equals(adapter.getAttribute(httpServletRequest, ServletConstants.SERVLET_ASYNC_FLAG))) {
            httpServletResponse = adapter.wrapResponse(httpServletResponse);
            return Pair.of(null, httpServletResponse);
        }

        CaseListenerImpl.INSTANCE.onEvent(new CaseEvent(StringUtil.isEmpty(caseId) ? StringUtil.EMPTY : caseId, CaseEvent.Action.CREATE));
        if (ContextManager.needRecordOrReplay()) {
            httpServletRequest = adapter.wrapRequest(httpServletRequest);
            httpServletResponse = adapter.wrapResponse(httpServletResponse);
            return Pair.of(httpServletRequest, httpServletResponse);
        }

        return null;
    }


    public static <TRequest, TResponse> void onServiceExit(
            ServletAdapter<TRequest, TResponse> adapter, Object servletRequest,
            Object servletResponse) {
        try {
            if (!ContextManager.needRecordOrReplay()) {
                return;
            }

            TRequest httpServletRequest = adapter.asHttpServletRequest(servletRequest);
            TResponse httpServletResponse = adapter.asHttpServletResponse(servletResponse);

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

    public static <TRequest, TResponse> void onInvokeForRequestExit(
            ServletAdapter<TRequest, TResponse> adapter, NativeWebRequest nativeWebRequest,
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

        TRequest httpServletRequest = adapter.getNativeRequest(nativeWebRequest);

        if (httpServletRequest == null) {
            return;
        }

        adapter.setAttribute(httpServletRequest, ServletConstants.SERVLET_RESPONSE, response);
    }

    public static boolean checkRateLimit(String path) {
        return ConfigManager.INSTANCE.isForceRecord() ||
                HealthManager.acquire(path, ConfigManager.INSTANCE.getRecordRate());
    }

    private static <TRequest> boolean shouldSkip(ServletAdapter<TRequest, ?> adapter,
                                                 TRequest httpServletRequest) {
        // Do nothing if request header with arex-replay-warm-up
        if (Boolean.parseBoolean(adapter.getRequestHeader(httpServletRequest, ServletConstants.REPLAY_WARM_UP))) {
            return true;
        }
        String contentType = adapter.getContentType(httpServletRequest);
        return isFilteredContentType(contentType) ||
                isCssPath(adapter.getServletPath(httpServletRequest));
    }

    private static boolean isFilteredContentType(String contentType) {
        return StringUtil.isEmpty(contentType) && FILTERED_CONTENT_TYPE.contains(contentType);
    }

    private static boolean isCssPath(String path) {
        return StringUtil.isEmpty(path) || path.endsWith(".css");
    }
}