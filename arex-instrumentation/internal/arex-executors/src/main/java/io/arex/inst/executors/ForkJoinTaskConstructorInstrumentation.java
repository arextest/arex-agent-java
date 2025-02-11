package io.arex.inst.executors;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.ctx.ArexThreadLocal;
import io.arex.agent.bootstrap.internal.Cache;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class ForkJoinTaskConstructorInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("java.util.concurrent.ForkJoinTask");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(isConstructor(),
                    "io.arex.inst.executors.ForkJoinTaskConstructorInstrumentation$ConstructorAdvice")
        );
    }

    @SuppressWarnings("unused")
    public static class ConstructorAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.This Object task) {
            if (TraceContextManager.get() == null) {
                return;
            }
            final Object captured = ArexThreadLocal.Transmitter.capture();
            if (captured != null) {
                Cache.CAPTURED_CACHE.put(task, captured);
            }
        }
    }
}
