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

/**
 * Replays the context captured when the task was constructed, and drops the captured entry
 * once the task has run.
 *
 * <p>Removing at the exec/run exit is safe. For {@code CompletableFuture} nodes,
 * {@code Completion.run()} and {@code exec()} are final methods on the base class and both are
 * just {@code tryFire(ASYNC)}; the ASYNC branch skips {@code claim()}, runs the function, and
 * then nulls out {@code src}, {@code dep} and {@code fn}, so it is that node's one terminal
 * fire and any later {@code tryFire} returns at the null check on entry. Same shape on JDK 8
 * and 21. For plain ForkJoinTasks, {@code doExec()} calls {@code exec()} once.
 *
 * <p>What this does <em>not</em> cover: the constructor advice captures for every ForkJoinTask,
 * but only tasks that reach exec/run get their entry dropped explicitly. Completions built by
 * the non-async operators ({@code thenApply}, {@code thenCompose}, ...) have a null executor and
 * are only ever driven through {@code tryFire(SYNC)} / {@code tryFire(NESTED)};
 * {@code CompletableFuture$Signaller} is captured too but is never submitted to the pool. Those
 * still wait for the weak key to be collected. There is no safe removal signal inside
 * {@code tryFire}: a null return means both "spun without firing" and "fired with no dependent
 * to propagate", and {@code Completion.isLive()} is package-private so inlined advice cannot
 * reach it. Binding the snapshot to a field on the task itself would remove the map entirely
 * and cover those cases, at the cost of adding a field-injection mechanism.
 *
 * <p>Known behaviour change: after {@code ForkJoinTask.reinitialize()} the same instance can be
 * forked again, and the second execution no longer replays. It previously replayed the snapshot
 * taken at construction time, which was already the wrong context for a fresh run. The JDK never
 * calls {@code reinitialize()} itself.
 */
public class ForkJoinTaskInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return hasSuperType(named("java.util.concurrent.ForkJoinTask"));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(
                new MethodInstrumentation(isMethod().and(namedOneOf("exec", "run")).and(not(isAbstract())),
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

        /**
         * {@code onThrowable} is required, not optional. Throwing out of exec is a normal path,
         * not a corner case: {@code RunnableExecuteAction.exec()} is a bare {@code runnable.run()},
         * {@code AdaptedCallable.exec()} rethrows explicitly, and {@code AdaptedXxx.run()} is
         * {@code invoke()}, which reports the exception by rethrowing it -- so the run and exec
         * exits are both skipped. Without it the enter advice has already replayed a context onto
         * this ForkJoin worker and nothing restores it, so the worker picks up the next task still
         * carrying the previous trace and its downstream calls are recorded under a foreign
         * traceId. The remove below would be skipped on the same paths.
         */
        @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
        public static void onExit(
                @Advice.This Object task,
                @Advice.Local("backup") Object backup) {
            ArexThreadLocal.Transmitter.restore(backup);
            // A non-null backup means captured was non-null on entry, because replay(null)
            // always returns null. Use it to skip the map lookup when nothing was recorded:
            // the constructor advice is gated on TraceContextManager and never put an entry in
            // that case, and exec is one of the hottest methods in the JVM.
            if (backup != null) {
                Cache.CAPTURED_CACHE.remove(task);
            }
        }
    }
}
