package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.runtime.model.Constants;
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
    public static final String SERVLET_ASYNC_FLAG = "arex-async-flag";
    public static final String SERVLET_RESPONSE = "arex-servlet-response";
    private static final Set<String> FILTERED_CONTENT_TYPE = new HashSet<>();
    private static final Set<String> FILTERED_GET_URL_SUFFIX = new HashSet<>();
    private static final String HTTP_METHOD_GET = "GET";

    static {
        FILTERED_CONTENT_TYPE.add("/javascript");
        FILTERED_CONTENT_TYPE.add("image/");
        FILTERED_CONTENT_TYPE.add("/font");
        FILTERED_CONTENT_TYPE.add("/pdf");
        FILTERED_CONTENT_TYPE.add("/css");

        FILTERED_GET_URL_SUFFIX.add(".js");
        FILTERED_GET_URL_SUFFIX.add(".css");
        FILTERED_GET_URL_SUFFIX.add(".png");
        FILTERED_GET_URL_SUFFIX.add(".woff");
        FILTERED_GET_URL_SUFFIX.add(".pdf");
        FILTERED_GET_URL_SUFFIX.add(".map");
        FILTERED_GET_URL_SUFFIX.add(".ico");
    }

    public static <TRequest, TResponse> Pair<TRequest, TResponse> onServiceEnter(
            ServletAdapter<TRequest, TResponse> adapter, TRequest httpServletRequest,
            TResponse httpServletResponse) {
        // Async listener will handle if attr with arex-async-flag
        if ("true".equals(adapter.getAttribute(httpServletRequest, SERVLET_ASYNC_FLAG))) {
            httpServletResponse = adapter.wrapResponse(httpServletResponse);
            return Pair.of(null, httpServletResponse);
        }

        if (shouldSkip(adapter, httpServletRequest)) {
            return null;
        }

        String caseId = adapter.getRequestHeader(httpServletRequest, ArexConstants.RECORD_ID);
        String excludeMockTemplate = adapter.getRequestHeader(httpServletRequest, ArexConstants.HEADER_EXCLUDE_MOCK);
        if (ContextManager.currentContext(true, caseId) != null) {
            CaseEventDispatcher.onEvent(new CaseEvent("", CaseEvent.Action.ENTER));
            httpServletRequest = adapter.wrapRequest(httpServletRequest);
            httpServletResponse = adapter.wrapResponse(httpServletResponse);
            return Pair.of(httpServletRequest, httpServletResponse);
        }

        return null;
    }


    public static <TRequest, TResponse> void onServiceExit(
            ServletAdapter<TRequest, TResponse> adapter, TRequest httpServletRequest,
            TResponse httpServletResponse) {
        try {
            if (!adapter.wrapped(httpServletRequest, httpServletResponse)) {
                return;
            }

            if (200 != adapter.getStatus(httpServletResponse)) {
                adapter.copyBodyToResponse(httpServletResponse);
                return;
            }

            // Async listener will handle async request
            if ("true".equals(adapter.getAttribute(httpServletRequest, SERVLET_ASYNC_FLAG))) {
                adapter.removeAttribute(httpServletRequest, SERVLET_ASYNC_FLAG);
                adapter.copyBodyToResponse(httpServletResponse);
                return;
            }

            // Add async listener for async request
            if (adapter.isAsyncStarted(httpServletRequest)) {
                adapter.setAttribute(httpServletRequest, SERVLET_ASYNC_FLAG, "true");
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
        if (response == null || !ContextManager.needRecordOrReplay()) {
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

        adapter.setAttribute(httpServletRequest, SERVLET_RESPONSE, response);
    }

    private static <TRequest> boolean shouldSkip(ServletAdapter<TRequest, ?> adapter,
                                                 TRequest httpServletRequest) {
        String caseId = adapter.getRequestHeader(httpServletRequest, Constants.RECORD_ID);
        String caseId = adapter.getRequestHeader(httpServletRequest, ArexConstants.RECORD_ID);

        // Replay scene
        if (StringUtil.isNotEmpty(caseId)) {
            return false;
        }

        String forceRecord = adapter.getRequestHeader(httpServletRequest, ArexConstants.FORCE_RECORD);
        // Do not skip if header with arex-force-record=true
        if (Boolean.parseBoolean(forceRecord)) {
            return false;
        }

        // Skip if request header with arex-replay-warm-up=true
        if (Boolean.parseBoolean(adapter.getRequestHeader(httpServletRequest, ArexConstants.REPLAY_WARM_UP))) {
            return true;
        }

        // Filter invalid servlet path suffix
        if (HTTP_METHOD_GET.equals(adapter.getMethod(httpServletRequest))) {
            String servletPath = adapter.getServletPath(httpServletRequest);
            return StringUtil.isEmpty(servletPath) || FILTERED_GET_URL_SUFFIX.stream().anyMatch(servletPath::contains);
        }

        // Filter invalid content-type
        String contentType = adapter.getContentType(httpServletRequest);
        if (StringUtil.isEmpty(contentType) || FILTERED_CONTENT_TYPE.stream().anyMatch(contentType::contains)) {
            return true;
        }

        String uri = adapter.getRequestURI(httpServletRequest);
        return RecordLimiter.acquire(uri);
    }
}