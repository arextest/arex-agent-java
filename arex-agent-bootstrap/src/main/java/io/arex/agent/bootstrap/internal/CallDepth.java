package io.arex.agent.bootstrap.internal;

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

    public static CallDepth simple() {
        return new CallDepth();
    }

    public static CallDepth forClass(String className) {
        try {
            return forClass(Class.forName(className, false, Thread.currentThread().getContextClassLoader()));
        } catch (Throwable ex) {
            return null;
        }
    }

    public int getAndIncrement() {
        return this.depth++;
    }

    public int decrementAndGet() {
        return --this.depth;
    }

    public CallDepth copy() {
        CallDepth copy = new CallDepth();
        copy.depth = this.depth;
        return copy;
    }
}
