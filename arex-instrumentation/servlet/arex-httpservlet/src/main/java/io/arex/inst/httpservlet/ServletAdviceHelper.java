package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.internal.Pair;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.EventProcessor;
import io.arex.inst.runtime.request.RequestHandlerManager;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.listener.EventSource;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.log.LogManager;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.method.support.InvocableHandlerMethod;

/**
 * ServletAdviceHelper
 */
public class ServletAdviceHelper {
    public static final String SERVLET_ASYNC_FLAG = "arex-async-flag";
    public static final String SERVLET_RESPONSE = "arex-servlet-response";
    private static final Set<String> FILTERED_CONTENT_TYPE = new HashSet<>();
    private static final Set<String> FILTERED_GET_URL_SUFFIX = new HashSet<>();
    public static final String PROCESSED_FLAG = "arex-processed-flag";

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

    /**
     * If there is an implementation of Filter, it is called by XXXFilter.
     * If there is no implementation of Filter, it is called by Servlet.service
     * <pre>
     * JavaWeb process:
     *                             filter1                      filter2
     *                       ----------------------       ----------------------
     *  ----------           |    ----------      |       |    ----------      |
     *  | client |--request--|--> | before |      |   ┎---|--> | before |      |
     *  ----------           |    ----------      |   ¦   |    ----------      |
     *       ↑               |         ↓          |   ¦   |         ↓          |
     *       ¦               |  ----------------  |   ¦   |  ----------------  |    -------------------
     *       ¦               |  |Chain.doFilter|--|---┘   |  |Chain.doFilter| -|--->| Servlet.service |
     *       ¦               |  ----------------  |       |  ----------------  |    -------------------
     *       ¦               |                    |       |                    |             ¦
     *       ¦               |    ----------      |       |    ----------      |             ¦
     *       ------response--|----| after  | <----|-------|----| after  | <----|-------------┘
     *                       |    ----------      |       |    ----------      |
     *                       ----------------------       ----------------------
     * </pre>
     */
    public static <TRequest, TResponse> Pair<TRequest, TResponse> onServiceEnter(
            ServletAdapter<TRequest, TResponse> adapter, Object servletRequest,
            Object servletResponse) {
        TRequest httpServletRequest = adapter.asHttpServletRequest(servletRequest);
        if (httpServletRequest == null) {
            return null;
        }

        // This judgment prevents multiple calls (although there are multiple calls in a request, only one pass is allowed)
        if (adapter.markProcessed(httpServletRequest, PROCESSED_FLAG)) {
            return null;
        }

        TResponse httpServletResponse = adapter.asHttpServletResponse(servletResponse);
        if (httpServletResponse == null) {
            return null;
        }

        // Async listener will handle if attr with arex-async-flag
        if (Boolean.TRUE.equals(adapter.getAttribute(httpServletRequest, SERVLET_ASYNC_FLAG))) {
            return null;
        }

        if (shouldSkip(adapter, httpServletRequest)) {
            CaseEventDispatcher.onEvent(CaseEvent.ofEnterEvent());
            return null;
        }

        RequestHandlerManager.preHandle(httpServletRequest, adapter.getServletVersion());
        // skip servlet if attr with arex-skip-flag
        if (Boolean.TRUE.equals(adapter.getAttribute(httpServletRequest, ArexConstants.SKIP_FLAG))) {
            return null;
        }

        // 302 Redirect request
        String redirectRecordId = getRedirectRecordId(adapter, httpServletRequest);
        if (StringUtil.isNotEmpty(redirectRecordId)) {
            TraceContextManager.set(redirectRecordId);
        } else {
            CaseEventDispatcher.onEvent(CaseEvent.ofEnterEvent());
            String caseId = adapter.getRequestHeader(httpServletRequest, ArexConstants.RECORD_ID);
            String excludeMockTemplate = adapter.getRequestHeader(httpServletRequest, ArexConstants.HEADER_EXCLUDE_MOCK);
            CaseEventDispatcher.onEvent(CaseEvent.ofCreateEvent(EventSource.of(caseId, excludeMockTemplate)));
            addAttachmentsToContext(adapter, httpServletRequest);
            RequestHandlerManager.handleAfterCreateContext(httpServletRequest, adapter.getServletVersion());
        }

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
            TRequest httpServletRequest = adapter.asHttpServletRequest(servletRequest);
            TResponse httpServletResponse = adapter.asHttpServletResponse(servletResponse);

            RequestHandlerManager.postHandle(httpServletRequest, httpServletResponse, adapter.getServletVersion());

            if (httpServletRequest == null || httpServletResponse == null) {
                return;
            }

            adapter.removeAttribute(httpServletRequest, PROCESSED_FLAG);

            if (!adapter.wrapped(httpServletRequest, httpServletResponse)) {
                return;
            }

            // Do not record if arex trace was removed
            if (!ContextManager.needRecordOrReplay()) {
                adapter.copyBodyToResponse(httpServletResponse);
                return;
            }

            // Async listener will handle async request
            if (Boolean.TRUE.equals(adapter.getAttribute(httpServletRequest, SERVLET_ASYNC_FLAG))) {
                return;
            }

            // Add async listener for async request
            if (adapter.isAsyncStarted(httpServletRequest)) {
                adapter.setAttribute(httpServletRequest, SERVLET_ASYNC_FLAG, Boolean.TRUE);
                adapter.addListener(adapter, httpServletRequest, httpServletResponse);
                return;
            }

            // sync request
            new ServletExtractor<>(adapter, httpServletRequest, httpServletResponse).execute();
        } catch (Throwable e) {
            LogManager.warn("servlet.onExit", e);
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
        if (invocableHandlerMethod.getReturnType().getMethodAnnotation(ResponseBody.class) == null &&
                !invocableHandlerMethod.getBeanType().isAnnotationPresent(RestController.class)) {
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
        if (!EventProcessor.dependencyInitComplete()) {
            return true;
        }

        // skip if pre-request http-method is HEAD or OPTIONS
        if (ArexConstants.HTTP_METHOD_HEAD.equals(adapter.getMethod(httpServletRequest))
                || ArexConstants.HTTP_METHOD_OPTIONS.equals(adapter.getMethod(httpServletRequest))) {
            return true;
        }

        String caseId = adapter.getRequestHeader(httpServletRequest, ArexConstants.RECORD_ID);

        // Replay scene
        if (StringUtil.isNotEmpty(caseId)) {
            return Config.get().getBoolean(ConfigConstants.DISABLE_REPLAY, false);
        }

        String forceRecord = adapter.getRequestHeader(httpServletRequest, ArexConstants.FORCE_RECORD, ArexConstants.HEADER_X_PREFIX);
        // Do not skip if header with arex-force-record=true
        if (Boolean.parseBoolean(forceRecord)) {
            return false;
        }

        // Skip if request header with arex-replay-warm-up=true
        if (Boolean.parseBoolean(adapter.getRequestHeader(httpServletRequest, ArexConstants.REPLAY_WARM_UP))) {
            return true;
        }

        String requestURI = adapter.getRequestURI(httpServletRequest);
        if (StringUtil.isEmpty(requestURI)) {
            return false;
        }
        String pattern = adapter.getPattern(httpServletRequest);
        // As long as one parameter is hit in includeServiceOperations, the operation will not be skipped
        if (CollectionUtil.isNotEmpty(Config.get().getIncludeServiceOperations()) &&
            !(IgnoreUtils.includeOperation(pattern) ||
                IgnoreUtils.includeOperation(requestURI))) {
            return true;
        }
        // As long as one parameter is hit in excludeServiceOperations, the operation will be skipped
        if (IgnoreUtils.excludeOperation(pattern) ||
            IgnoreUtils.excludeOperation(requestURI)) {
            return true;
        }

        // Filter invalid servlet path suffix
        if (FILTERED_GET_URL_SUFFIX.stream().anyMatch(requestURI::endsWith)) {
            return true;
        }

        // Filter invalid content-type
        String contentType = adapter.getContentType(httpServletRequest);
        if (StringUtil.isNotEmpty(contentType) && FILTERED_CONTENT_TYPE.stream().anyMatch(contentType::contains)) {
            return true;
        }

        return Config.get().invalidRecord(pattern);
    }

    private static <TRequest, TResponse> String getRedirectRecordId(ServletAdapter<TRequest, TResponse> adapter,
        TRequest httpServletRequest) {
        String redirectRecordId = adapter.getParameterFromQueryString(httpServletRequest, ArexConstants.RECORD_ID);
        if (StringUtil.isEmpty(redirectRecordId)) {
            return null;
        }

        String referer = adapter.getRequestHeader(httpServletRequest, "referer");

        if (StringUtil.isEmpty(referer)) {
            return null;
        }

        ArexContext context = ContextManager.getContext(redirectRecordId);
        if (context != null && context.isRedirectRequest(referer)) {
            return redirectRecordId;
        }

        return null;
    }

    private static <TRequest, TResponse> void addAttachmentsToContext(ServletAdapter<TRequest, TResponse> adapter, TRequest request) {
        ContextManager.setAttachment(ArexConstants.FORCE_RECORD, adapter.getRequestHeader(request, ArexConstants.FORCE_RECORD, ArexConstants.HEADER_X_PREFIX));
        ContextManager.setAttachment(ArexConstants.SCHEDULE_REPLAY, adapter.getRequestHeader(request, ArexConstants.SCHEDULE_REPLAY));
    }
}
