package io.arex.inst.executors;

import io.arex.agent.bootstrap.ctx.ArexThreadLocal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}