package io.arex.inst.database.mongo;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.OnNonDefaultValue;
import net.bytebuddy.asm.Advice.Return;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ResourceManagerInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.mongodb.internal.operation.QueryBatchCursor$ResourceManager");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(isMethod().and(not(named("execute"))), SkipAdvice.class.getName()));
    }

    public static class SkipAdvice {
        @OnMethodEnter(skipOn = OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
           return ContextManager.needReplay();
        }

        @OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Origin Method method,
                @Return(readOnly = false, typing = Typing.DYNAMIC) Object result) {
            if (ContextManager.needReplay() && boolean.class.isAssignableFrom(method.getReturnType())) {
                result = true;
            }
        }
    }
}
