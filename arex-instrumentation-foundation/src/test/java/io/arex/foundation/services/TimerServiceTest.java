package io.arex.foundation.services;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2024/1/15
 */
class TimerServiceTest {

    @Test
    void schedule() {
        ScheduledFuture<?> scheduleTest = TimerService.schedule(() -> System.out.println("schedule test"), 0,
            TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(scheduleTest);
        scheduleTest.cancel(true);
    }

    @Test
    void scheduleAtFixedRate() throws InterruptedException {
        AtomicInteger runs = new AtomicInteger();
        ScheduledFuture<?> scheduleTest = TimerService.scheduleAtFixedRate(() -> {
                if (runs.get() >= 1) {
                    throw new RuntimeException("scheduleAtFixedRate test stop");
                }
                System.out.println("scheduleAtFixedRate test");
                runs.getAndIncrement();
            }, 0,
            50, TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(scheduleTest);
        scheduleTest.cancel(true);
        TimeUnit.MILLISECONDS.sleep(150);
    }
}
