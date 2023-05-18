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
        assertNotNull(RunnableWrapper.get(new AdaptedRunnable<>()));
        assertNotNull(RunnableWrapper.get(() -> {}));
        TraceContextManager.remove();
    }

    static class AdaptedRunnable<T> extends ForkJoinTask<T> implements RunnableFuture<T> {
        public final T getRawResult() { return null; }
        public final void setRawResult(T v) {}
        public final boolean exec() { return true; }
        public final void run() {}
    }
}