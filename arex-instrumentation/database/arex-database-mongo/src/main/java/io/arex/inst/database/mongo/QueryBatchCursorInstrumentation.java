package io.arex.inst.database.mongo;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class QueryBatchCursorInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return namedOneOf("com.mongodb.internal.operation.QueryBatchCursor", "com.mongodb.operation.QueryBatchCursor");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(isMethod().and(named("close")), SkipAdvice.class.getName()));
    }

    public static class SkipAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return ContextManager.needReplay();
        }
    }
}
