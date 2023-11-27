package io.arex.inst.cache.guava;

import com.google.common.cache.CacheLoader;
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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class GuavaCacheInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.google.common.cache.LocalCache").and(not(isInterface().or(isAbstract())));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Arrays.asList(
                new MethodInstrumentation(isMethod().and(named("get")).and(takesArguments(2)),
                GetAdvice.class.getName()),
                new MethodInstrumentation(isMethod().and(named("getIfPresent")).and(takesArguments(1)),
                        GetAdvice.class.getName()));
    }

    public static class GetAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class ,suppress = Throwable.class)
        public static boolean onEnter(@Advice.Origin("#m") String methodName,
                                      @Advice.Argument(0) Object key,
                                      @Advice.FieldValue("defaultLoader") CacheLoader loader,
                                      @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }

            if (ContextManager.needReplay()) {
                String className = CacheLoaderUtil.getLocatedClass(loader);
                DynamicClassExtractor extractor = new DynamicClassExtractor(className, methodName, new Object[]{key});
                mockResult = extractor.replay();
                return mockResult != null && mockResult.notIgnoreMockResult();
            }

            return false;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Origin("#m") String methodName,
                                  @Advice.Argument(0) Object key,
                                  @Advice.FieldValue("defaultLoader") CacheLoader loader,
                                  @Advice.Local("mockResult") MockResult mockResult,
                                  @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
                                  @Advice.Thrown(readOnly = false) Throwable throwable) {
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = mockResult.getResult();
                }
            }

            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
                String className = CacheLoaderUtil.getLocatedClass(loader);
                DynamicClassExtractor extractor = new DynamicClassExtractor(className, methodName, new Object[]{key});
                extractor.recordResponse(throwable != null ? throwable : result);
            }
        }
    }
}
