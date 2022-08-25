package io.arex.inst.httpservlet.inst;

import io.arex.agent.bootstrap.DecorateControl;
import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.inst.httpservlet.ServletAdviceHelper;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.InvocableHandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static io.arex.foundation.matcher.DecorateSwitchMatcher.decorateSwitch;
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
    public InvocableHandlerInstrumentationV3(ModuleDescription module) {
        super(module);
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return decorateSwitch(named("org.springframework.web.method.support.InvocableHandlerMethod"),
                DecorateControl.ServletVersion3Switch.class);
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("invokeForRequest").and(isPublic())
            .and(takesArgument(0, named("org.springframework.web.context.request.NativeWebRequest")))
            .and(takesArgument(1, named("org.springframework.web.method.support.ModelAndViewContainer")));

        String adviceClassName = this.getClass().getName() + "$InvokeAdvice";

        return singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    @Override
    public List<String> adviceClassNames() {
        return singletonList("io.arex.inst.httpservlet.inst.InvocableHandlerInstrumentationV3$InvokeAdvice");
    }

    public static class InvokeAdvice {
        public static final ServletAdapter<HttpServletRequest, HttpServletResponse> ADAPTER =
            ServletAdapterImplV3.getInstance();

        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(0) NativeWebRequest nativeWebRequest,
            @Advice.This InvocableHandlerMethod invocableHandlerMethod, @Advice.Return Object response) {
            ServletAdviceHelper.onInvokeForRequestExit(ADAPTER, nativeWebRequest, invocableHandlerMethod, response);
        }
    }
}
