package io.arex.inst.executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.inst.executors.FutureTaskInstrumentation.CallableAdvice;
import io.arex.inst.executors.FutureTaskInstrumentation.RunnableAdvice;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FutureTaskInstrumentationTest {

    FutureTaskInstrumentation inst = new FutureTaskInstrumentation();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void typeMatcher() {
        assertTrue(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(FutureTask.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(2, inst.methodAdvices().size());
    }

    @Test
    void CallableAdvice_methodEnter() {
        Callable<String> callable = () -> "mock-test";
        assertDoesNotThrow(() -> CallableAdvice.methodEnter(callable));
    }

    @Test
    void RunnableAdvice_methodEnter() {
        Runnable runnable = () -> System.out.println("mock-test");
        assertDoesNotThrow(() -> RunnableAdvice.methodEnter(runnable));
    }
}
