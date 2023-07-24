package io.arex.inst.dubbo.lexin;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * DubboInstrumentation
 */
public class DubboConsumerInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.alibaba.dubbo.rpc.cluster.support.wrapper.MockClusterInvoker");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("invoke")
                .and(takesArgument(0, named("com.alibaba.dubbo.rpc.Invocation")));

        return singletonList(new MethodInstrumentation(matcher, InvokeAdvice.class.getName()));
    }

    public static class InvokeAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.This Invoker<?> invoker,
                                      @Advice.Argument(0) Invocation invocation,
                                      @Advice.Local("extractor") DubboConsumerExtractor extractor,
                                      @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecordOrReplay()) {
                RepeatedCollectManager.enter();
                extractor = new DubboConsumerExtractor(DubboAdapter.of(invoker, invocation));
                if (ContextManager.needReplay()) {
                    mockResult = extractor.replay();
                }
            }
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Return(readOnly = false) Result result,
                                  @Advice.Local("extractor") DubboConsumerExtractor extractor,
                                  @Advice.Local("mockResult") MockResult mockResult) {
            if (extractor == null || !RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                result = (Result) mockResult.getResult();
                return;
            }

            if (ContextManager.needRecord()) {
                extractor.record(result);
            }
        }
    }
}
