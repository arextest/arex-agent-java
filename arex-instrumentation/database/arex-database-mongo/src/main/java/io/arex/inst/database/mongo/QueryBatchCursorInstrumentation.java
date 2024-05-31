package io.arex.inst.database.mongo;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class QueryBatchCursorInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return namedOneOf("com.mongodb.internal.operation.QueryBatchCursor", "com.mongodb.operation.QueryBatchCursor");
    }

    /**
     * query process:
     * 1. for each -> hasNext -> next, if hasNext return false, indicates that all data has been returned.
     * 2. first -> hasNext -> next, get the first data, then close the cursor.
     */
    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Arrays.asList(new MethodInstrumentation(isMethod().and(named("close")), CloseAdvice.class.getName()),
                new MethodInstrumentation(isMethod().and(named("hasNext")), HashNextAdvice.class.getName()),
                new MethodInstrumentation(isMethod().and(named("next")), NextAdvice.class.getName()));
    }

    public static class CloseAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return ContextManager.needReplay();
        }
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.This Object thiz) {
            if (ContextManager.needRecord()) {
                MongoHelper.recordFindOperation(thiz.hashCode());
            }
        }
    }

    public static class HashNextAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.This Object thiz,
                                  @Advice.Return boolean hasNext) {
            if (ContextManager.needRecord() && !hasNext) {
                MongoHelper.recordFindOperation(thiz.hashCode());
            }
        }
    }

    public static class NextAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.This Object thiz,
                                  @Advice.Return List next) {
            if (ContextManager.needRecord()) {
                MongoHelper.addNextBatchList(thiz.hashCode(), next);
            }
        }
    }
}
