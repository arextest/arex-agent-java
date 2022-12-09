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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

import static io.arex.inst.extension.matcher.SafeExtendsClassMatcher.extendsClass;
import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * ServletInstrumentationV3
 *
 * @date 2022/03/03
 */
public class ServletInstrumentationV3 extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return extendsClass(named("javax.servlet.http.HttpServlet"), false);
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher =
                named("service").and(isProtected()).and(takesArgument(0, named("javax.servlet.http.HttpServletRequest")))
                        .and(takesArgument(1, named("javax.servlet.http.HttpServletResponse")));

        String adviceClassName = this.getClass().getName() + "$ServiceAdvice";

        return Collections.singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    @Override
    public List<String> adviceClassNames() {
        return asList(
                "io.arex.inst.httpservlet.adapter.ServletAdapter",
                "io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3",
                "io.arex.inst.httpservlet.ServletAdviceHelper",
                "io.arex.inst.httpservlet.listener.ServletAsyncListenerV3",
                "io.arex.inst.httpservlet.wrapper.CachedBodyRequestWrapperV3",
                "io.arex.inst.httpservlet.wrapper.CachedBodyResponseWrapperV3",
                "io.arex.inst.httpservlet.wrapper.CachedBodyRequestWrapperV3$ContentCachingInputStream",
                "io.arex.inst.httpservlet.wrapper.CachedBodyResponseWrapperV3$ResponseServletOutputStream",
                "io.arex.inst.httpservlet.wrapper.CachedBodyResponseWrapperV3$ResponsePrintWriter",
                "io.arex.inst.httpservlet.ServletExtractor");
    }

    public static class ServiceAdvice {
        /*public static final ServletAdapter<HttpServletRequest, HttpServletResponse> ADAPTER =
            ServletAdapterImplV3.getInstance();*/

        @Advice.OnMethodEnter
        public static void onEnter(@Advice.Argument(value = 0, readOnly = false) HttpServletRequest request,
            @Advice.Argument(value = 1, readOnly = false) HttpServletResponse response) throws ServletException {
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

        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(value = 0, readOnly = false) HttpServletRequest request,
            @Advice.Argument(value = 1, readOnly = false) HttpServletResponse response) {
            ServletAdviceHelper.onServiceExit(ServletAdapterImplV3.getInstance(), request, response);
        }
    }
}
