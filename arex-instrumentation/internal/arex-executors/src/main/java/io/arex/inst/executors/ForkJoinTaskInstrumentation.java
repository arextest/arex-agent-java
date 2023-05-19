package io.arex.inst.executors;

import io.arex.agent.bootstrap.ctx.ArexThreadLocal;
import io.arex.agent.bootstrap.internal.Cache;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ForkJoinTaskInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return hasSuperType(named("java.util.concurrent.ForkJoinTask"));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(
                new MethodInstrumentation(isMethod().and(named("exec")).and(not(isAbstract())),
                "io.arex.inst.executors.ForkJoinTaskInstrumentation$ExecAdvice"));
    }

    @SuppressWarnings("unused")
    public static class ExecAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.This Object task,
                @Advice.Local("backup") Object backup) {
            final Object captured = Cache.CAPTURED_CACHE.get(task);
            backup = ArexThreadLocal.Transmitter.replay(captured);
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Local("backup") Object backup) {
            ArexThreadLocal.Transmitter.restore(backup);
        }
    }
}
