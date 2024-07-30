package io.arex.inst.runtime.context;

import java.util.function.Function;

public class RecordLimiter {
    private static RecordLimiter INSTANCE = null;

    private Function<String, Boolean> recordLimiter;

    RecordLimiter(Function<String, Boolean> limiter) {
        this.recordLimiter = limiter;
    }

    public static void init(Function<String, Boolean> limiter) {
        INSTANCE = new RecordLimiter(limiter);
    }

    public static boolean acquire(String path) {
//        return INSTANCE.recordLimiter.apply(path);
        return true;
    }
}
