package io.arex.agent.bootstrap.ctx;

import io.arex.agent.bootstrap.TraceContextManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RunnableFuture;

import static org.junit.jupiter.api.Assertions.*;

class RunnableWrapperTest {

    @Test
    void get() throws Exception {
        assertNull(RunnableWrapper.get(null));
        TraceContextManager.set("mock");
        Runnable objectRunnable = RunnableWrapper.get(new RunnableTest<>());
        assertNotNull(objectRunnable);
        Runnable emptyRunnable = RunnableWrapper.get(() -> {});
        assertDoesNotThrow(emptyRunnable::run);
        assertNotNull(emptyRunnable.toString());
        assertTrue(emptyRunnable.hashCode() > 0);
        assertNotEquals(emptyRunnable, objectRunnable);

        NoRunnableFieldPriorityRunnable noRunnableFieldComparableRunnable = new NoRunnableFieldPriorityRunnable(1);
        RunnableWrapper.get(noRunnableFieldComparableRunnable);
        Field comparableRunnableFieldMap = RunnableWrapper.class.getDeclaredField("COMPARABLE_RUNNABLE_FIELD_MAP");
        comparableRunnableFieldMap.setAccessible(true);
        Map<String, Optional<Field>> map = (Map<String, Optional<Field>>) comparableRunnableFieldMap.get(null);
        assertNotNull(map);
        assertFalse(map.get(noRunnableFieldComparableRunnable.getClass().getName()).isPresent());
        Runnable originalRunnable = () -> {};
        PriorityRunnable priorityRunnable = new PriorityRunnable(originalRunnable, 1);
        Runnable runnable2 = RunnableWrapper.get(priorityRunnable);
        assertSame(priorityRunnable, runnable2);
        assertEquals("run", map.get(priorityRunnable.getClass().getName()).get().getName());
        assertInstanceOf(RunnableWrapper.class, priorityRunnable.getRun());
        // runnable field already wrapped
        RunnableWrapper.get(priorityRunnable);
        Runnable run = priorityRunnable.getRun();
        assertInstanceOf(RunnableWrapper.class, run);
        Field runnable = RunnableWrapper.class.getDeclaredField("runnable");
        runnable.setAccessible(true);
        assertFalse(runnable.get(run) instanceof RunnableWrapper);

        TraceContextManager.remove();
    }

    static class RunnableTest<T> extends ForkJoinTask<T> implements RunnableFuture<T> {
        public final T getRawResult() { return null; }
        public final void setRawResult(T v) {}
        public final boolean exec() { return true; }
        public final void run() {}
    }

    public static class NoRunnableFieldPriorityRunnable implements Runnable, Comparable<NoRunnableFieldPriorityRunnable> {
        private final int priority;

        public NoRunnableFieldPriorityRunnable(int priority) {
            this.priority = priority;
        }

        @Override
        public int compareTo(NoRunnableFieldPriorityRunnable other) {
            int res = 0;
            if (this.priority != other.priority) {
                res = this.priority > other.priority ? -1 : 1;
            }
            return res;
        }

        @Override
        public void run() {
            System.out.println("PriorityRunnable.run");
        }
    }

    static class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {
        private final Runnable run;
        private final int priority;

        public PriorityRunnable(Runnable run, int priority) {
            this.run = run;
            this.priority = priority;
        }

        @Override
        public int compareTo(PriorityRunnable other) {
            int res = 0;
            if (this.priority != other.priority) {
                res = this.priority > other.priority ? -1 : 1;
            }
            return res;
        }

        @Override
        public void run() {
            this.run.run();
        }

        public Runnable getRun() {
            return run;
        }
    }
}
