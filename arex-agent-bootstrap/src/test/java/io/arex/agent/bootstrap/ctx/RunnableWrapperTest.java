package io.arex.agent.bootstrap.ctx;

import io.arex.agent.bootstrap.TraceContextManager;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RunnableFuture;

import static org.junit.jupiter.api.Assertions.*;

class RunnableWrapperTest {

    @Test
    void get() {
        assertNull(RunnableWrapper.get(null));
        TraceContextManager.set("mock");
        Runnable objectRunnable = RunnableWrapper.get(new RunnableTest<>());
        assertNotNull(objectRunnable);
        Runnable emptyRunnable = RunnableWrapper.get(() -> {});
        assertDoesNotThrow(emptyRunnable::run);
        assertNotNull(emptyRunnable.toString());
        assertTrue(emptyRunnable.hashCode() > 0);
        assertFalse(emptyRunnable.equals(objectRunnable));
        TraceContextManager.remove();
    }

    static class RunnableTest<T> extends ForkJoinTask<T> implements RunnableFuture<T> {
        public final T getRawResult() { return null; }
        public final void setRawResult(T v) {}
        public final boolean exec() { return true; }
        public final void run() {}
    }
}
