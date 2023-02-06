package io.arex.inst.executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.inst.executors.ThreadInstrumentation.StartAdvice;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ThreadInstrumentationTest {
    ThreadInstrumentation inst = new ThreadInstrumentation();

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertTrue(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(Thread.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(1, inst.methodAdvices().size());
    }

    @Test
    void StartAdvice_methodEnter() {
        Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock-case-id"));
        Runnable runnable = () -> System.out.println("mock-test");
        assertDoesNotThrow(() -> StartAdvice.methodEnter(runnable));
    }
}
