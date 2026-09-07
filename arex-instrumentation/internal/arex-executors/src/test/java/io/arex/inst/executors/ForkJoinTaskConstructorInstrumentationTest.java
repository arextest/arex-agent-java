package io.arex.inst.executors;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.ctx.ArexThreadLocal;
import io.arex.agent.bootstrap.internal.Cache;
import io.arex.agent.bootstrap.util.VirtualThreadUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ForkJoinTaskConstructorInstrumentationTest {

    static ForkJoinTaskConstructorInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new ForkJoinTaskConstructorInstrumentation();
        Mockito.mockStatic(ArexThreadLocal.Transmitter.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @Test
    void methodAdvices() {
        assertNotNull(target.methodAdvices());
    }

    @Test
    void onEnter() {
        Mockito.when(ArexThreadLocal.Transmitter.capture()).thenReturn("mock");
        assertDoesNotThrow(() -> ForkJoinTaskConstructorInstrumentation.ConstructorAdvice.onExit(null));
    }

    @Test
    void onExit_skipCarrierThread() {
        try (MockedStatic<VirtualThreadUtil> mocked = Mockito.mockStatic(VirtualThreadUtil.class);
             MockedStatic<TraceContextManager> traces = Mockito.mockStatic(TraceContextManager.class)) {
            mocked.when(VirtualThreadUtil::isCarrierThread).thenReturn(true);
            traces.when(TraceContextManager::get).thenReturn("carrier-context");
            Object task = new Object();
            assertDoesNotThrow(() -> ForkJoinTaskConstructorInstrumentation.ConstructorAdvice.onExit(task));
            traces.verifyNoInteractions();
            // must not touch the weak cache(ReferenceQueue) on a carrier thread
            assertFalse(Cache.CAPTURED_CACHE.contains(task));
        }
    }
}
