package io.arex.inst.httpservlet.inst;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpservlet.ServletAdviceHelper;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV5;
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

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * FilterInstrumentationV5
 */
public class FilterInstrumentationV5 extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return not(isInterface()).and(hasSuperType(named("jakarta.servlet.Filter")));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("doFilter")
                .and(takesArgument(0, named("jakarta.servlet.ServletRequest")))
                .and(takesArgument(1, named("jakarta.servlet.ServletResponse")));

        return Collections.singletonList(new MethodInstrumentation(matcher, FilterAdvice.class.getName()));
    }

    @Override
    public List<String> adviceClassNames() {
        return asList(
                "io.arex.inst.httpservlet.adapter.ServletAdapter",
                "io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV5",
                "io.arex.inst.httpservlet.ServletAdviceHelper",
                "io.arex.inst.httpservlet.listener.ServletAsyncListenerV5",
                "io.arex.inst.httpservlet.wrapper.CachedBodyRequestWrapperV5",
                "io.arex.inst.httpservlet.wrapper.CachedBodyResponseWrapperV5",
                "io.arex.inst.httpservlet.wrapper.CachedBodyRequestWrapperV5$ContentCachingInputStream",
                "io.arex.inst.httpservlet.wrapper.CachedBodyResponseWrapperV5$ResponseServletOutputStream",
                "io.arex.inst.httpservlet.wrapper.CachedBodyResponseWrapperV5$ResponsePrintWriter",
                "io.arex.inst.httpservlet.ServletExtractor",
                "io.arex.inst.httpservlet.wrapper.FastByteArrayOutputStream");
    }

    public static class FilterAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(value = 0, readOnly = false) ServletRequest request,
                                   @Advice.Argument(value = 1, readOnly = false) ServletResponse response) {
            Pair<HttpServletRequest, HttpServletResponse> pair =
                    ServletAdviceHelper.onServiceEnter(ServletAdapterImplV5.getInstance(), request, response);

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
            ServletAdviceHelper.onServiceExit(ServletAdapterImplV5.getInstance(), request, response);
        }
    }
}
