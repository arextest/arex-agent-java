package io.arex.foundation.internal;

public class CallDepth {

    private static final ClassValue<ThreadLocal<CallDepth>> DEPTH_TL =
            new ClassValue<ThreadLocal<CallDepth>>() {
                @Override
                protected ThreadLocal<CallDepth> computeValue(Class<?> type) {
                        return ThreadLocal.withInitial(() -> new CallDepth());
                    }
                };

    private int depth;

    CallDepth() {
        this.depth = 0;
    }

    public static CallDepth forClass(Class<?> clazz) {
        return DEPTH_TL.get(clazz).get();
    }

    public int getAndIncrement() {
        return this.depth++;
    }

    public int decrementAndGet() {
        return --this.depth;
    }
}
