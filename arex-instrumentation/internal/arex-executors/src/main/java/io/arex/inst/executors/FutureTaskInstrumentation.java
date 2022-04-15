package io.arex.inst.executors;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.agent.bootstrap.ctx.CallableWrapper;
import io.arex.agent.bootstrap.ctx.RunnableWrapper;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class FutureTaskInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("java.util.concurrent.FutureTask");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return asList(buildCallableAdvice(),
                buildRunnableAdvice());
    }

    private MethodInstrumentation buildCallableAdvice() {
        return new MethodInstrumentation(
                isConstructor().and(takesArguments(1))
                        .and(takesArgument(0, Callable.class)),
                this.getClass().getName() + "$CallableAdvice");
    }

    private MethodInstrumentation buildRunnableAdvice() {
        return new MethodInstrumentation(
                isConstructor().and(takesArguments(2))
                        .and(takesArgument(0, Runnable.class)),
                this.getClass().getName() + "$RunnableAdvice");
    }

    @SuppressWarnings("unused")
    public static class CallableAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void methodEnter(
                @Advice.Argument(value = 0, readOnly = false) Callable<?> callable) {
            callable = CallableWrapper.get(callable);
        }
    }

    @SuppressWarnings("unused")
    public static class RunnableAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void methodEnter(
                @Advice.Argument(value = 0, readOnly = false) Runnable runnable) {
            runnable = RunnableWrapper.get(runnable);
        }
    }
}
