package io.arex.inst.database.mybatis3;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;
import org.apache.ibatis.executor.Executor;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public class DefaultSqlSessionInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.ibatis.session.defaults.DefaultSqlSession");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(
                isConstructor()
                        .and(takesArguments(3))
                        .and(takesArgument(0, named("org.apache.ibatis.session.Configuration")))
                        .and(takesArgument(1, named("org.apache.ibatis.executor.Executor"))),
                this.getClass().getName() + "$ConstructorAdvice"));
    }

    @SuppressWarnings("unused")
    public static class ConstructorAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.FieldValue(value = "executor", readOnly = false) Executor executor) {
            if (ContextManager.needRecordOrReplay()) {
                executor = ExecutorWrapper.get(executor);
            }
        }
    }
}
