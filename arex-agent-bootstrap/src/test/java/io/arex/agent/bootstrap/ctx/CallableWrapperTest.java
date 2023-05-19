package io.arex.agent.bootstrap.ctx;

import io.arex.agent.bootstrap.TraceContextManager;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RunnableFuture;

import static org.junit.jupiter.api.Assertions.*;

class CallableWrapperTest {

    @Test
    void get() {
        assertNull(CallableWrapper.get(null));
        TraceContextManager.set("mock");
        assertNotNull(CallableWrapper.get(new CallableTest<>()));
        assertNotNull(CallableWrapper.get(() -> "mock"));
        TraceContextManager.remove();
    }

    static class CallableTest<T> extends ForkJoinTask<T> implements Callable<T> {
        public final T getRawResult() { return null; }
        public final void setRawResult(T v) {}
        public final boolean exec() { return true; }
        public final T call() { return null; }
    }
}