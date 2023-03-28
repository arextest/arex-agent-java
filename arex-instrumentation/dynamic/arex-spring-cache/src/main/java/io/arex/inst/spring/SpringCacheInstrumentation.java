package io.arex.inst.spring;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class SpringCacheInstrumentation extends TypeInstrumentation {


    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.springframework.cache.interceptor.CacheAspectSupport");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
       return Collections.singletonList(new MethodInstrumentation(named("execute").and(isProtected()).and(takesArguments(4)),
               SpringCacheAdvice.class.getName()));
    }

    @SuppressWarnings("unused")
    public static class SpringCacheAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.Argument(2) Method method,
                                      @Advice.Argument(3) Object[] args,
                                      @Advice.Local("extractor") DynamicClassExtractor extractor,
                                      @Advice.Local("mockResult") MockResult mockResult) {

            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            if (SpringCacheAdviceHelper.needRecordOrReplay(method)) {
                extractor = SpringCacheAdviceHelper.createDynamicExtractor(method, args);
                if (ContextManager.needReplay()) {
                    mockResult = extractor.replay();
                }
            }
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Local("extractor") DynamicClassExtractor extractor,
                                  @Advice.Local("mockResult") MockResult mockResult,
                                  @Advice.Thrown(readOnly = false) Throwable throwable,
                                  @Advice.Return(readOnly = false) Object result) {
            // replay
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                    return;
                }
                result = mockResult.getResult();
                return;
            }

            // record
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate() && extractor != null) {
                extractor.setResponse(result);
            }
        }
    }
}
