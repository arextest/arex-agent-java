package io.arex.inst.cache.caffeine;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.cache.util.CacheLoaderUtil;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class CaffeineAsyncInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.github.benmanes.caffeine.cache.LocalAsyncLoadingCache");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Arrays.asList(new MethodInstrumentation(
                isMethod().and(named("get")).and(takesArguments(3)),
                AsyncCacheAdvice.class.getName()),
                new MethodInstrumentation(isMethod().and(named("getIfPresent")).and(takesArguments(1)),
                        AsyncCacheAdvice.class.getName()),
                new MethodInstrumentation(isMethod().and(named("getAll")).and(takesArguments(1)),
                        AsyncCacheAdvice.class.getName()));
    }

    public static class AsyncCacheAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class, skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(@Advice.Argument(0) Object key,
                                      @Advice.Origin("#m") String methodName,
                                      @Advice.Origin("#r") String methodReturnType,
                                      @Advice.Local("mockResult") MockResult mockResult,
                                      @Advice.FieldValue("loader") Object cacheLoader) {
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            if (ContextManager.needReplay() && CacheLoaderUtil.needRecordOrReplay(cacheLoader)) {
                String className = CacheLoaderUtil.getLocatedClass(cacheLoader);
                DynamicClassExtractor extractor = new DynamicClassExtractor(className, methodName, new Object[]{key}, methodReturnType);
                mockResult = extractor.replayOrRealCall();
                return mockResult != null && mockResult.notIgnoreMockResult();
            }
            return false;
        }
        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Origin("#m") String methodName,
                                  @Advice.Argument(0) Object key,
                                  @Advice.Local("mockResult") MockResult mockResult,
                                  @Advice.FieldValue("loader") Object cacheLoader,
                                  @Advice.Origin("#r") String methodReturnType,
                                  @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
                                  @Advice.Thrown(readOnly = false) Throwable throwable) {
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = mockResult.getResult();
                }
                return;
            }
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate() && CacheLoaderUtil.needRecordOrReplay(cacheLoader)) {
                String className = CacheLoaderUtil.getLocatedClass(cacheLoader);
                DynamicClassExtractor extractor = new DynamicClassExtractor(className, methodName, new Object[]{key}, methodReturnType);
                extractor.recordResponse(throwable != null ? throwable : result);
            }
        }
    }
}
