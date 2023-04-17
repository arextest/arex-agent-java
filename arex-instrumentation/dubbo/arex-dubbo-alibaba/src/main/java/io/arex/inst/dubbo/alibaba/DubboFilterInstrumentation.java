package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * DubboProviderInstrumentation
 */
public class DubboFilterInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return not(isInterface()).and(hasSuperType(named("com.alibaba.dubbo.rpc.Filter")));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("invoke")
                .and(takesArgument(1, named("com.alibaba.dubbo.rpc.Invocation")));

        return singletonList(new MethodInstrumentation(matcher, InvokeAdvice.class.getName()));
    }

    public static class InvokeAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Argument(1) Invocation invocation,
                                  @Advice.Return(readOnly = false) Result result) {
            DubboFilterExtractor.setResultAttachment(invocation, result);
        }
    }
}
