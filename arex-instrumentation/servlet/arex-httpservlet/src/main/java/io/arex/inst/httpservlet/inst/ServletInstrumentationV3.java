package io.arex.inst.httpservlet.inst;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.internal.Pair;
import io.arex.inst.httpservlet.ServletAdviceHelper;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

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
 * ServletInstrumentationV3
 *
 * @date 2022/03/03
 */
public class ServletInstrumentationV3 extends TypeInstrumentation {
    public ServletInstrumentationV3(ModuleDescription module) {
        super(module);
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("javax.servlet.http.HttpServlet");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher =
            named("service").and(isPublic()).and(takesArgument(0, named("javax.servlet.ServletRequest")))
                .and(takesArgument(1, named("javax.servlet.ServletResponse")));

        String adviceClassName = this.getClass().getName() + "$ServiceAdvice";

        return Collections.singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    public static class ServiceAdvice {
        public static final ServletAdapter<HttpServletRequest, HttpServletResponse> ADAPTER =
            ServletAdapterImplV3.getInstance();

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
