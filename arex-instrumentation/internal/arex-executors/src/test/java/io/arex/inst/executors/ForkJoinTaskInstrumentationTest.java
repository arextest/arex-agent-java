package io.arex.inst.executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.arex.agent.bootstrap.ctx.ArexThreadLocal;
import io.arex.agent.bootstrap.internal.Cache;
import io.arex.inst.executors.ForkJoinTaskInstrumentation.ExecAdvice;
import java.lang.reflect.Method;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinTask;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ForkJoinTaskInstrumentationTest {

    ForkJoinTaskInstrumentation inst = new ForkJoinTaskInstrumentation();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        Cache.CAPTURED_CACHE.clear();
    }

    @Test
    void typeMatcher() {
        boolean matched1 = inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(ForkJoinTask.class));
        boolean matched2 = inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(CountedCompleter.class));
        assertTrue(matched1 & matched2);
    }

    @Test
    void methodAdvices() {
        assertEquals(1, inst.methodAdvices().size());
    }

    @Test
    void ExecAdvice_onEnter() {
        assertDoesNotThrow(() -> ExecAdvice.onEnter("fork-test", new Object()));
    }

    @Test
    void ExecAdvice_onExit() {
        assertDoesNotThrow(() -> ExecAdvice.onExit("fork-test", ArexThreadLocal.Transmitter.capture()));
    }

    @Test
    void ConstructorAdvice_onEnter() {
        assertDoesNotThrow(() -> ForkJoinTaskConstructorInstrumentation.ConstructorAdvice.onExit(new Object()));
    }

    /**
     * The exit advice must drop the captured entry. Leaving it behind is the bug: the snapshot is
     * the map value, so it stays strongly reachable from the static cache and is promoted to the
     * old generation, and nothing evicts it until a later put/get drains the reference queue.
     */
    @Test
    void execAdviceRemovesCapturedEntryOnExit() {
        ArexThreadLocal<String> threadLocal = new ArexThreadLocal<>();
        try {
            Object task = new Object();
            Cache.CAPTURED_CACHE.put(task, "snapshot");
            assertTrue(Cache.CAPTURED_CACHE.contains(task));

            ExecAdvice.onExit(task, backupOf(threadLocal));

            assertFalse(Cache.CAPTURED_CACHE.contains(task));
        } finally {
            threadLocal.remove();
        }
    }

    /**
     * A null backup means captured was null on entry, i.e. nothing was being recorded and the
     * constructor advice never put an entry. Touching the map there would cost a hash and a bin
     * walk on one of the hottest methods in the JVM for nothing. Dropping the guard fails this.
     */
    @Test
    void execAdviceSkipsRemoveWhenNothingWasReplayed() {
        Object task = new Object();
        Cache.CAPTURED_CACHE.put(task, "snapshot");

        ExecAdvice.onExit(task, null);

        assertTrue(Cache.CAPTURED_CACHE.contains(task));
    }

    /**
     * Throwing out of exec is a normal path: RunnableExecuteAction.exec() is a bare
     * runnable.run(). Without onThrowable the whole exit advice is skipped, the replayed context
     * is never restored, and the worker carries the previous trace into its next task.
     */
    @Test
    void exitAdviceAlsoRunsOnTheExceptionPath() {
        for (Method method : ExecAdvice.class.getDeclaredMethods()) {
            Advice.OnMethodExit exit = method.getAnnotation(Advice.OnMethodExit.class);
            if (exit != null) {
                assertEquals(Throwable.class, exit.onThrowable(),
                        "ExecAdvice.onExit is missing onThrowable: when the instrumented method "
                                + "throws, the exit advice does not run and the replayed context "
                                + "leaks onto the worker thread.");
                return;
            }
        }
        fail("ExecAdvice has no @Advice.OnMethodExit method");
    }

    /**
     * Builds a non-null backup. Transmitter.capture() returns null when no ArexThreadLocal is
     * registered in the holder, so one has to be set first -- otherwise the test would silently
     * slide onto the "nothing recorded" branch and assert nothing. restore(capture()) is an
     * identity operation for the current thread, so this does not disturb other tests.
     */
    private static Object backupOf(ArexThreadLocal<String> threadLocal) {
        threadLocal.set("value");
        Object backup = ArexThreadLocal.Transmitter.capture();
        assertNotNull(backup);
        return backup;
    }
}
