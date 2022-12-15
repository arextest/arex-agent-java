package io.arex.inst.dubbo;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * DubboProviderInstrumentation
 */
public class DubboProviderInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.dubbo.rpc.proxy.AbstractProxyInvoker");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("invoke")
                .and(takesArgument(0, named("org.apache.dubbo.rpc.Invocation")));

        String adviceClassName = this.getClass().getName() + "$InvokeAdvice";

        return singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    public static class InvokeAdvice {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.This Invoker<?> invoker,
                                   @Advice.Argument(0) Invocation invocation) {
            DubboProviderExtractor.onServiceEnter(invoker, invocation);
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.This Invoker<?> invoker,
                                  @Advice.Argument(0) Invocation invocation,
                                  @Advice.Return(readOnly = false) Result result) {
            DubboProviderExtractor.onServiceExit(invoker, invocation, result);
        }
    }
}
