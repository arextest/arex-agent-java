package io.arex.inst.executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.inst.executors.ThreadPoolInstrumentation.ExecutorCallableAdvice;
import io.arex.inst.executors.ThreadPoolInstrumentation.ExecutorRunnableAdvice;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThreadPoolInstrumentationTest {
    ThreadPoolInstrumentation inst = new ThreadPoolInstrumentation();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void typeMatcher() {
        assertTrue(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(ForkJoinPool.class)));
        assertTrue(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(ThreadPoolExecutor.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(2, inst.methodAdvices().size());
    }

    @Test
    void ExecutorCallableAdvice_methodEnter() {
        Callable<String> callable = () -> "mock-test";
        assertDoesNotThrow(() -> ExecutorCallableAdvice.methodEnter(callable));
    }

    @Test
    void ExecutorRunnableAdvice_methodEnter() {
        Runnable runnable = () -> System.out.println("mock-test");
        assertDoesNotThrow(() -> ExecutorRunnableAdvice.methodEnter(runnable));
    }
}
