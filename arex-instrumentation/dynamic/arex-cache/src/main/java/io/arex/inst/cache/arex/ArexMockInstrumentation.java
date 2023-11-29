package io.arex.inst.cache.arex;

import static net.bytebuddy.matcher.ElementMatchers.inheritsAnnotation;
import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.returns;

import com.arextest.common.annotation.ArexMock;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.dynamic.common.DynamiConstants;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;

public class ArexMockInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return inheritsAnnotation(named(DynamiConstants.AREX_MOCK));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        Junction<MethodDescription> matcher = isMethod()
            .and(not(returns(TypeDescription.VOID)))
            .and(isAnnotatedWith(named(DynamiConstants.AREX_MOCK)))
            .and(not(isAnnotatedWith(named(DynamiConstants.SPRING_CACHE))));
        MethodInstrumentation method = new MethodInstrumentation(matcher,
            ArexMockAdvice.class.getName());

        return Collections.singletonList(method);
    }

    @SuppressWarnings("unused")
    public static class ArexMockAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.Origin Method method,
            @Advice.AllArguments Object[] args,
            @Advice.Local("extractor") DynamicClassExtractor extractor,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecordOrReplay()) {
                ArexMock arexMock = method.getDeclaredAnnotation(ArexMock.class);
                String keyExpression = null;
                Class<?> actualType = null;
                if (arexMock != null) {
                    keyExpression = arexMock.key();
                    actualType = arexMock.actualType();
                }
                extractor = new DynamicClassExtractor(method, args, keyExpression, actualType);
            }
            if (ContextManager.needReplay()) {
                mockResult = extractor.replay();
                return mockResult != null && mockResult.notIgnoreMockResult();
            }
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            return false;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Local("extractor") DynamicClassExtractor extractor,
            @Advice.Local("mockResult") MockResult mockResult,
            @Advice.Thrown(readOnly = false) Throwable throwable,
            @Advice.Return(readOnly = false, typing = Typing.DYNAMIC) Object result) {
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = mockResult.getResult();
                }
                return;
            }
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate() && extractor != null) {
                result = extractor.recordResponse(throwable != null ? throwable : result);
            }
        }
    }
}
