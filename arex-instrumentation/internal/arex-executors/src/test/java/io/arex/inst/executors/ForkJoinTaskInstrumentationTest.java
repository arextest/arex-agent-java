package io.arex.inst.executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.agent.bootstrap.ctx.ArexThreadLocal;
import io.arex.agent.bootstrap.internal.Cache;
import io.arex.inst.executors.ForkJoinTaskInstrumentation.ConstructorAdvice;
import io.arex.inst.executors.ForkJoinTaskInstrumentation.ExecAdvice;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinTask;
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
    }

    @Test
    void typeMatcher() {
        boolean matched1 = inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(ForkJoinTask.class));
        boolean matched2 = inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(CountedCompleter.class));
        assertTrue(matched1 & matched2);
    }

    @Test
    void methodAdvices() {
        assertEquals(2, inst.methodAdvices().size());
    }

    @Test
    void ExecAdvice_onEnter() {
        Cache.CAPTURED_CACHE.put("fork-test", ArexThreadLocal.Transmitter.capture());
        assertDoesNotThrow(() -> ExecAdvice.onEnter("fork-test", new Object()));
    }

    @Test
    void ExecAdvice_onExit() {
        assertDoesNotThrow(() -> ExecAdvice.onExit(ArexThreadLocal.Transmitter.capture()));
    }

    @Test
    void ConstructorAdvice_onEnter() {
        assertDoesNotThrow(() -> ConstructorAdvice.onExit(new Object()));
    }
}
