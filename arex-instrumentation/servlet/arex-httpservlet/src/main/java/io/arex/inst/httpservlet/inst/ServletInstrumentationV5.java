package io.arex.inst.httpservlet.inst;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.internal.Pair;
import io.arex.inst.httpservlet.ServletAdviceHelper;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV5;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * ServletInstrumentation
 *
 * @date 2022/03/03
 */
public class ServletInstrumentationV5 extends TypeInstrumentation {

    public ServletInstrumentationV5(ModuleDescription module) {
        super(module);
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("jakarta.servlet.http.HttpServlet");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher =
            named("service").and(isPublic()).and(takesArgument(0, named("jakarta.servlet.ServletRequest")))
                .and(takesArgument(1, named("jakarta.servlet.ServletResponse")));

        String adviceClassName = this.getClass().getName() + "$ServiceAdvice";

        return Collections.singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    public static class ServiceAdvice {
        public static final ServletAdapter<HttpServletRequest, HttpServletResponse> ADAPTER =
            ServletAdapterImplV5.getInstance();

        @Advice.OnMethodEnter
        public static void onEnter(@Advice.Argument(value = 0, readOnly = false) ServletRequest request,
            @Advice.Argument(value = 1, readOnly = false) ServletResponse response) throws ServletException {
            Pair<HttpServletRequest, HttpServletResponse> pair =
                ServletAdviceHelper.onServiceEnter(ADAPTER, request, response);

            if (pair == null) {
                return;
            }

            if (pair.getFirst() != null) {
                request = pair.getFirst();
            }

            if (pair.getSecond() != null) {
                response = pair.getSecond();
            }
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(value = 0, readOnly = false) ServletRequest request,
            @Advice.Argument(value = 1, readOnly = false) ServletResponse response) {
            ServletAdviceHelper.onServiceExit(ADAPTER, request, response);
        }
    }
}
