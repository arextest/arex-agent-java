package io.arex.inst.cache.spring;

import static net.bytebuddy.matcher.ElementMatchers.isProtected;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.cache.util.CacheLoaderUtil;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.cache.annotation.Cacheable;

public class SpringCacheInstrumentation extends TypeInstrumentation {


    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.springframework.cache.interceptor.CacheAspectSupport");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(
            new MethodInstrumentation(named("execute").and(isProtected()).and(takesArguments(4)),
                SpringCacheAdvice.class.getName()));
    }

    @SuppressWarnings("unused")
    public static class SpringCacheAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.Argument(2) Method method,
            @Advice.Argument(3) Object[] args,
            @Advice.Local("extractor") DynamicClassExtractor extractor,
            @Advice.Local("mockResult") MockResult mockResult) {
            // only record and replay non-void method
            if (Void.TYPE.equals(method.getReturnType())) {
                return false;
            }

            if (CacheLoaderUtil.needRecordOrReplay(method)) {
                Cacheable cacheable = method.getDeclaredAnnotation(Cacheable.class);
                String keyExpression = cacheable != null ? cacheable.key() : null;
                extractor = new DynamicClassExtractor(method, args, keyExpression, null);
            }
            if (extractor != null && ContextManager.needReplay()) {
                mockResult = extractor.replay();
                return mockResult != null && mockResult.notIgnoreMockResult();
            }
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            return false;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Argument(2) Method method,
            @Advice.Local("extractor") DynamicClassExtractor extractor,
            @Advice.Local("mockResult") MockResult mockResult,
            @Advice.Thrown(readOnly = false) Throwable throwable,
            @Advice.Return(readOnly = false) Object result) {
            // only record and replay non-void method
            if (Void.TYPE.equals(method.getReturnType())) {
                return;
            }

            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = mockResult.getResult();
                }
                return;
            }
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate() && extractor != null) {
                extractor.recordResponse(throwable != null ? throwable : result);
            }
        }
    }
}
