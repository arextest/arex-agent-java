package io.arex.inst.httpservlet.inst;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpservlet.ServletAdviceHelper;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.InvocableHandlerMethod;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * InvocableHandlerInstrumentationV3
 *
 * @date 2022/03/10
 */
public class InvocableHandlerInstrumentationV3 extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.springframework.web.method.support.InvocableHandlerMethod");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("invokeForRequest").and(isPublic())
            .and(takesArgument(0, named("org.springframework.web.context.request.NativeWebRequest")))
            .and(takesArgument(1, named("org.springframework.web.method.support.ModelAndViewContainer")));

        String adviceClassName = this.getClass().getName() + "$InvokeAdvice";

        return singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    public static class InvokeAdvice {

        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(0) NativeWebRequest nativeWebRequest,
            @Advice.This InvocableHandlerMethod invocableHandlerMethod, @Advice.Return Object response) {
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

            HttpServletRequest httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

            if (httpServletRequest == null) {
                return;
            }

            ADAPTER.setAttribute(httpServletRequest, ServletAdviceHelper.SERVLET_RESPONSE, response);
        }
    }
}
