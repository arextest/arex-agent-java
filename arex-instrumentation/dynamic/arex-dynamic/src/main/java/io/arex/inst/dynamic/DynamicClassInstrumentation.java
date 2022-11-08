package io.arex.inst.dynamic;

import io.arex.foundation.context.RepeatedCollectManager;
import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * DynamicClassInstrumentation
 */
public class DynamicClassInstrumentation extends TypeInstrumentation {
    private final String dynamicClass;

    public DynamicClassInstrumentation(String dynamicClass) {
        this.dynamicClass = dynamicClass;
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named(dynamicClass);
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(
                isMethod().and(isPublic()).
                        and(not(isConstructor()))
                        .and(not(takesNoArguments()))
                        .and(not(returns(TypeDescription.VOID))),
                MethodAdvice.class.getName()));
    }

    public final static class MethodAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(@Advice.Origin("#t") String className) {
            RepeatedCollectManager.enter();
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Origin("#t") String className, @Advice.Origin("#m") String methodName,
                                  @Advice.AllArguments Object[] args,
                                  @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result) {
            if (!RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            DynamicClassExtractor extractor;
            if (ContextManager.needReplay()) {
                extractor = new DynamicClassExtractor(className, methodName, args);
                result = extractor.replay();
                return;
            }

            if (ContextManager.needRecord()) {
                extractor = new DynamicClassExtractor(className, methodName, args, result);
                extractor.record();
            }
        }
    }
}