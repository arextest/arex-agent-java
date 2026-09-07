package io.arex.inst.executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.agent.bootstrap.ctx.ArexThreadLocal;
import io.arex.agent.bootstrap.util.VirtualThreadUtil;
import io.arex.inst.executors.ForkJoinTaskInstrumentation.ExecAdvice;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinTask;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

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
        assertEquals(1, inst.methodAdvices().size());
    }

    @Test
    void ExecAdvice_onEnter() {
        assertDoesNotThrow(() -> ExecAdvice.onEnter("fork-test", new Object()));
    }

    @Test
    void ExecAdvice_onEnter_skipCarrierThread() {
        try (MockedStatic<VirtualThreadUtil> mocked = Mockito.mockStatic(VirtualThreadUtil.class);
             MockedStatic<ArexThreadLocal.Transmitter> transmitter = Mockito.mockStatic(ArexThreadLocal.Transmitter.class)) {
            mocked.when(VirtualThreadUtil::isCarrierThread).thenReturn(true);
            assertDoesNotThrow(() -> ExecAdvice.onEnter("fork-test", new Object()));
            // must not touch thread locals(nor the weak cache/ReferenceQueue) on a carrier thread
            transmitter.verify(() -> ArexThreadLocal.Transmitter.replay(Mockito.any()), Mockito.never());
        }
    }

    @Test
    void ExecAdvice_onExit() {
        assertDoesNotThrow(() -> ExecAdvice.onExit(ArexThreadLocal.Transmitter.capture()));
    }

    @Test
    void ConstructorAdvice_onEnter() {
        assertDoesNotThrow(() -> ForkJoinTaskConstructorInstrumentation.ConstructorAdvice.onExit(new Object()));
    }
}
