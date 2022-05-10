package io.arex.inst.executors;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.util.LogUtil;
import io.arex.agent.bootstrap.ctx.RunnableWrapper;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class ThreadInstrumentation extends TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return is(Thread.class);
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(buildStartAdvice());
    }

    private MethodInstrumentation buildStartAdvice() {
        return new MethodInstrumentation(
                isMethod().and(named("start")).and(takesArguments(0)),
                this.getClass().getName() + "$StartAdvice");
    }

    @SuppressWarnings("unused")
    public static class StartAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void methodEnter(
                @Advice.FieldValue(value = "target", readOnly = false) Runnable runnable) {
            LogUtil.warn("Thread Advice");
            ArexContext context = ContextManager.currentContext();
            if (context != null) {
                LogUtil.warn("Thread Advice Wrapped");
                runnable = RunnableWrapper.get(runnable);
            }
        }
    }
}
