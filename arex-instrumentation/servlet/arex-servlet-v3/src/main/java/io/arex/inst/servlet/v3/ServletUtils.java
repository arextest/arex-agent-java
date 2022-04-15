package io.arex.inst.servlet.v3;

import io.arex.foundation.context.ContextManager;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ServletUtils
 *
 *
 * @date 2022/03/03
 */
public class ServletUtils {
    private static final String RECORD_ID = "arex-record-id";
    private static final String REPLAY_ID = "arex-replay-id";
    private static final String AREX_ASYNC_FLAG = "arex-async-flag";
    private static final String AREX_SERVLET_RESPONSE = "arex-servlet-response";
    private static final String AREX_REPLAY_WARM_UP = "arex-replay-warm-up";

    /**
     * agent will call this method
     */
    public static String getCaseId(HttpServletRequest request) {
        return request.getHeader(RECORD_ID);
    }

    public static void setResponse(HttpServletResponse response) {
        setResponseHeader(response);
    }

    private static void setResponseHeader(HttpServletResponse response) {
        if (ContextManager.needRecord()) {
            response.setHeader(RECORD_ID, ContextManager.currentContext().getCaseId());
        }

        if (ContextManager.needReplay()) {
            response.setHeader(REPLAY_ID, ContextManager.currentContext().getReplayId());
        }
    }

    public static boolean replayWarmUp(HttpServletRequest request) {
        return Boolean.parseBoolean(request.getHeader(AREX_REPLAY_WARM_UP));
    }

    public static boolean containsArexAsyncFlag(ServletRequest request) {
        return "true".equals(request.getAttribute(AREX_ASYNC_FLAG));
    }

    public static void setArexAsyncFlag(ServletRequest request) {
        request.setAttribute(AREX_ASYNC_FLAG, "true");
    }

    public static void removeArexAsyncFlag(ServletRequest request) {
        request.removeAttribute(AREX_ASYNC_FLAG);
    }

    public static Object getServletResponse(ServletRequest request) {
        Object response = request.getAttribute(AREX_SERVLET_RESPONSE);
        removeServletResponse(request);
        return response;
    }

    public static void setServletResponse(ServletRequest request, Object response) {
        request.setAttribute(AREX_SERVLET_RESPONSE, response);
    }

    public static void removeServletResponse(ServletRequest request) {
        request.removeAttribute(AREX_SERVLET_RESPONSE);
    }
}
