package io.arex.inst.spring.data.redis;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.redis.common.RedisKeyUtil;
import java.util.List;
import java.util.Objects;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * OperationsInstrumentation
 */
public class OperationsInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return namedOneOf("org.springframework.data.redis.core.DefaultValueOperations",
            "org.springframework.data.redis.core.DefaultSetOperations",
            "org.springframework.data.redis.core.DefaultZSetOperations",
            "org.springframework.data.redis.core.DefaultListOperations",
            "org.springframework.data.redis.core.DefaultHashOperations");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return asList(MethodCollector.arg1IsObjectKey(OneKeyAdvice.class.getName()),
            MethodCollector.arg1IsCollectionKey(OneKeyAdvice.class.getName()),
            MethodCollector.arg1IsMapKey(OneKeyAdvice.class.getName()),
            MethodCollector.arg1IsObjectKeyArg2IsCollectionKey(TwoKeysAdvice.class.getName()),
            MethodCollector.arg1IsObjectKeyArg2IsMapKey(TwoKeysAdvice.class.getName()),
            MethodCollector.arg1AndArg2AreObjectKey(TwoKeysAdvice.class.getName()));
    }

    public static class OneKeyAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.FieldValue(value = "template") RedisTemplate template,
            @Advice.Origin("#m") String methodName,
            @Advice.Argument(0) Object key,
            @Advice.Local("mockResult") MockResult mockResult) {
            mockResult = RedisTemplateProvider.methodOnEnter(Objects.toString(template.getConnectionFactory()),
                methodName, RedisKeyUtil.generate(key));
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.FieldValue(value = "template") RedisTemplate template,
            @Advice.Origin("#m") String methodName,
            @Advice.Argument(0) Object key,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
            @Advice.Thrown(readOnly = false, typing = Assigner.Typing.DYNAMIC) Throwable throwable,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = mockResult.getResult();
                }
                return;
            }
            RedisTemplateProvider.methodOnExit(Objects.toString(template.getConnectionFactory()), methodName,
                RedisKeyUtil.generate(key), result, throwable);
        }
    }

    public static class TwoKeysAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.FieldValue(value = "template") RedisTemplate template,
            @Advice.Origin("#m") String methodName,
            @Advice.Argument(0) Object key,
            @Advice.Argument(1) Object otherKey,
            @Advice.Local("mockResult") MockResult mockResult) {
            mockResult = RedisTemplateProvider.methodOnEnter(Objects.toString(template.getConnectionFactory()),
                methodName, RedisKeyUtil.generate(key, otherKey));
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.FieldValue(value = "template") RedisTemplate template,
            @Advice.Origin("#m") String methodName,
            @Advice.Argument(0) Object key,
            @Advice.Argument(1) Object otherKey,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
            @Advice.Thrown(readOnly = false, typing = Assigner.Typing.DYNAMIC) Throwable throwable,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = mockResult.getResult();
                }
                return;
            }
            RedisTemplateProvider.methodOnExit(Objects.toString(template.getConnectionFactory()), methodName,
                RedisKeyUtil.generate(key, otherKey), result, throwable);
        }
    }
}
