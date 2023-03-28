package io.arex.inst.httpservlet.inst;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpservlet.ServletAdviceHelper;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * FilterInstrumentationV3
 */
public class FilterInstrumentationV3 extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return not(isInterface()).and(hasSuperType(named("javax.servlet.Filter")));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("doFilter")
                .and(takesArgument(0, named("javax.servlet.ServletRequest")))
                .and(takesArgument(1, named("javax.servlet.ServletResponse")));

        return Collections.singletonList(new MethodInstrumentation(matcher, FilterAdvice.class.getName()));
    }

    public static class FilterAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(value = 0, readOnly = false) ServletRequest request,
                                   @Advice.Argument(value = 1, readOnly = false) ServletResponse response) {
            Pair<HttpServletRequest, HttpServletResponse> pair =
                    ServletAdviceHelper.onServiceEnter(ServletAdapterImplV3.getInstance(), request, response);

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

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Argument(value = 0, readOnly = false) ServletRequest request,
                                  @Advice.Argument(value = 1, readOnly = false) ServletResponse response) {
            ServletAdviceHelper.onServiceExit(ServletAdapterImplV3.getInstance(), request, response);
        }
    }
}
