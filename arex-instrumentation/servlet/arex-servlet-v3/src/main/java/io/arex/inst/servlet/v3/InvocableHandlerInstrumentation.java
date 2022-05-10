package io.arex.inst.servlet.v3;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.method.support.InvocableHandlerMethod;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * InvocableHandlerInstrumentation
 *
 *
 * @date 2022/03/10
 */
public class InvocableHandlerInstrumentation extends TypeInstrumentation {
    public InvocableHandlerInstrumentation(ModuleDescription module) {
        super(module);
    }
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

        return Collections.singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    public static class InvokeAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
            @Advice.Argument(0) NativeWebRequest request,
            @Advice.This InvocableHandlerMethod invocableHandlerMethod,
            @Advice.Return Object response) {
            if (response == null) {
                return;
            }

            if (!ContextManager.needRecordOrReplay()) {
                return;
            }

            // Do not set when async request
            if (response instanceof CompletableFuture ||
                response instanceof DeferredResult ||
                response instanceof Callable) {
                return;
            }

            // Set response only when return response body
            if (!invocableHandlerMethod.getReturnType().hasMethodAnnotation(ResponseBody.class)) {
                return;
            }

            ServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);

            if (servletRequest == null) {
                return;
            }

            ServletUtils.setServletResponse(servletRequest, response);
        }
    }
}
