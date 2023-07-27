package io.arex.foundation.services;

import io.arex.foundation.util.httpclient.async.ThreadFactoryImpl;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimerService {

    private static final ScheduledThreadPoolExecutor SCHEDULER =
            new ScheduledThreadPoolExecutor(1, new ThreadFactoryImpl("timer-service"));

    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return SCHEDULER.schedule(command, delay, unit);
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return SCHEDULER.scheduleAtFixedRate(command, initialDelay, period, unit);
    }
}
