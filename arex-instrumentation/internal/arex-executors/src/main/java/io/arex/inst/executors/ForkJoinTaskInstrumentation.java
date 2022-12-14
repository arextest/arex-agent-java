package io.arex.inst.executors;

import io.arex.agent.bootstrap.ctx.ArexThreadLocal;
import io.arex.agent.bootstrap.internal.Cache;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class ForkJoinTaskInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return hasSuperType(named("java.util.concurrent.ForkJoinTask"));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return asList(buildConstructorMethodInstrumentation(),
                buildExecInstrumentation());
    }

    private MethodInstrumentation buildExecInstrumentation() {
        return new MethodInstrumentation(isMethod().and(named("exec")).and(not(isAbstract())),
                "io.arex.inst.executors.ForkJoinTaskInstrumentation$ExecAdvice");
    }

    private MethodInstrumentation buildConstructorMethodInstrumentation() {
        return new MethodInstrumentation(
                isConstructor(),
                "io.arex.inst.executors.ForkJoinTaskInstrumentation$ConstructorAdvice");
    }

    @SuppressWarnings("unused")
    public static class ExecAdvice {
        @Advice.OnMethodEnter
        public static void onEnter(
                @Advice.This Object task,
                @Advice.Local("backup") Object backup) {
            backup = ArexThreadLocal.Transmitter.replay(Cache.CAPTURED_CACHE.get(task));
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Local("backup") Object backup) {
            ArexThreadLocal.Transmitter.restore(backup);
        }
    }

    @SuppressWarnings("unused")
    public static class ConstructorAdvice {
        @Advice.OnMethodExit
        public static void onExit(@Advice.This Object task) {
            Cache.CAPTURED_CACHE.put(task, ArexThreadLocal.Transmitter.capture());
        }
    }
}
