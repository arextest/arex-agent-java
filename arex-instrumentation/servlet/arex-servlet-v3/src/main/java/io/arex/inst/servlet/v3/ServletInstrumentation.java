package io.arex.inst.servlet.v3;

import io.arex.api.instrumentation.MethodInstrumentation;
import io.arex.api.instrumentation.ModuleDescription;
import io.arex.api.instrumentation.TypeInstrumentation;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.util.LogUtil;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * ServletInstrumentation
 *
 *
 * @date 2022/03/03
 */
public class ServletInstrumentation extends TypeInstrumentation {
    public ServletInstrumentation(ModuleDescription module) {
        super(module);
    }
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("javax.servlet.http.HttpServlet");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("service").and(isPublic())
            .and(takesArgument(0, named("javax.servlet.ServletRequest")))
            .and(takesArgument(1, named("javax.servlet.ServletResponse")));

        String adviceClassName = this.getClass().getName() + "$ServiceAdvice";

        return Collections.singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    public static class ServiceAdvice {

        @Advice.OnMethodEnter
        public static void onEnter(@Advice.Argument(value = 0, readOnly = false) ServletRequest request,
            @Advice.Argument(value = 1, readOnly = false) ServletResponse response) throws ServletException {
            HttpServletRequest servletRequest;
            HttpServletResponse servletResponse;

            try {
                servletRequest = (HttpServletRequest) request;
                servletResponse = (HttpServletResponse) response;
            } catch (ClassCastException e) {
                throw new ServletException("non-HTTP request or response");
            }

            // Do nothing if request header with arex-replay-warm-up
            if (ServletUtils.replayWarmUp(servletRequest)) {
                return;
            }

            // Async listener will handle if attr with arex-async-flag
            if (ServletUtils.containsArexAsyncFlag(request)) {
                response = new CachedBodyResponseWrapper(servletResponse);
                return;
            }

            String caseId = ServletUtils.getCaseId(servletRequest);
            ContextManager.currentContext(true, caseId);
            if (ContextManager.needRecordOrReplay()) {
                request = new CachedBodyRequestWrapper(servletRequest);
                response = new CachedBodyResponseWrapper(servletResponse);
            }
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(value = 0, readOnly = false) ServletRequest request,
            @Advice.Argument(value = 1, readOnly = false) ServletResponse response) {
            try {
                if (!ContextManager.needRecordOrReplay()) {
                    return;
                }

                if (!(response instanceof CachedBodyResponseWrapper)
                    || !(request instanceof CachedBodyRequestWrapper)) {
                    return;
                }

                if (HttpStatus.SC_OK != ((CachedBodyResponseWrapper) response).getStatus()) {
                    ((CachedBodyResponseWrapper) response).copyBodyToResponse();
                    return;
                }

                // Async listener will handle async request
                if (ServletUtils.containsArexAsyncFlag(request)) {
                    ServletUtils.removeArexAsyncFlag(request);
                    ((CachedBodyResponseWrapper) response).copyBodyToResponse();
                    return;
                }

                // Add async listener for async request
                if (request.isAsyncStarted()) {
                    ServletUtils.setArexAsyncFlag(request);
                    request.getAsyncContext().addListener(new ServletAsyncListener(), request, response);
                    return;
                }

                // sync request
                new ServletWrapper(request, response).execute();
            } catch (Throwable e) {
                LogUtil.warn("servlet.onExit", e);
            }
        }
    }
}
