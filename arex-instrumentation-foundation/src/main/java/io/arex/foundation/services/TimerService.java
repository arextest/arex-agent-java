package io.arex.foundation.services;

import io.arex.foundation.util.async.ThreadFactoryImpl;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimerService {

    private static ScheduledThreadPoolExecutor scheduler =
            new ScheduledThreadPoolExecutor(1, new ThreadFactoryImpl("timer-service"));

    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduler.schedule(command, delay, unit);
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
    }
}
