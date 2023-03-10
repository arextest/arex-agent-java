package io.arex.inst.httpservlet.inst;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
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

        String adviceClassName = "io.arex.inst.httpservlet.inst.ServletInstrumentationV5$ServiceAdvice";

        return Collections.singletonList(new MethodInstrumentation(matcher, adviceClassName));
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
}
