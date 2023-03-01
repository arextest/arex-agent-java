package io.arex.foundation.healthy;

import io.arex.foundation.config.ConfigManager;
import io.arex.agent.bootstrap.internal.Pair;
import io.arex.foundation.services.TimerService;
import com.google.common.util.concurrent.RateLimiter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.arex.foundation.healthy.HealthManager.RecordRateManager.RECORD_RATE_MANAGER;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class HealthManager {

    static final int NORMAL = 0;

    /**
     * Fast reject
     */
    static final int FAST_REJECT = 1;

    /**
     * Queue overflow
     */
    static final int QUEUE_OVERFLOW = 2;

    /**
     * Storage service exception
     */
    static final int SERVICE_EXCEPTION = 3;

    private static final AtomicInteger STATE = new AtomicInteger(NORMAL);

    private static final RecordRateChecker DATA_SERVICE_CHECKER =
            new RecordRateChecker(TimeUnit.MILLISECONDS.toMillis(300));
    private static final RecordRateChecker BALANCE_CHECKER = new RecordRateChecker(TimeUnit.MILLISECONDS.toMillis(3000));
    private static ScheduledFuture<?> scheduledFuture = null;

    /**
     * Record rate acquire
     */
    public static boolean acquire(String uri) {
        return RECORD_RATE_MANAGER.acquire(uri, ConfigManager.INSTANCE.getRecordRate());
    }

    public static void onEnqueueRejection() {
        if (STATE.compareAndSet(NORMAL, FAST_REJECT)) {
            // Check state after 30 seconds
            scheduledFuture = TimerService.schedule(() -> {
                RECORD_RATE_MANAGER.decelerate();
                STATE.compareAndSet(FAST_REJECT, QUEUE_OVERFLOW);
                scheduledFuture = TimerService.schedule(new HealthCheckTask(), 5, MINUTES);
            }, 30, SECONDS);
        }
    }

    public static void onDataServiceRejection() {
        if (STATE.compareAndSet(NORMAL, FAST_REJECT) || STATE.compareAndSet(QUEUE_OVERFLOW, FAST_REJECT)) {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }

            // Check state after 10 seconds
            scheduledFuture = TimerService.schedule(() -> {
                STATE.compareAndSet(FAST_REJECT, SERVICE_EXCEPTION);
                scheduledFuture = TimerService.schedule(new HealthCheckTask(), 3, MINUTES);
            }, 10, SECONDS);
        }
    }

    public static boolean isFastRejection() {
        return STATE.get() == FAST_REJECT;
    }

    /**
     * Report time consuming
     * @param time -1,Reject;
     * @param isQueuedTime isQueuedTime
     */
    public static void reportUsedTime(long time, boolean isQueuedTime) {
        if (STATE.get() <= FAST_REJECT) {
            return;
        }

        if (isQueuedTime) {
            BALANCE_CHECKER.statistic(time);
        } else {
            DATA_SERVICE_CHECKER.statistic(time);
        }
    }

    static class HealthCheckTask implements Runnable {
        @Override
        public void run() {
            boolean isRecover = false;
            switch (STATE.get()) {
                case SERVICE_EXCEPTION:
                    isRecover = DATA_SERVICE_CHECKER.isRecover();
                    if (isRecover) {
                        RECORD_RATE_MANAGER.changeRate(false);
                        STATE.set(NORMAL);
                        scheduledFuture = null;
                    } else {
                        RECORD_RATE_MANAGER.changeRate(true);
                        scheduledFuture = TimerService.schedule(this, 60, MINUTES);
                    }
                    break;
                case QUEUE_OVERFLOW:
                    isRecover = BALANCE_CHECKER.isRecover();
                    if (isRecover) {
                        STATE.set(NORMAL);
                        scheduledFuture = null;
                    } else {
                        RECORD_RATE_MANAGER.decelerate();
                        scheduledFuture = TimerService.schedule(this, 10, MINUTES);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    static class RecordRateChecker {
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fatal = new AtomicInteger(0);
        long defaultTimeout;

        RecordRateChecker(long timeout) {
            this.defaultTimeout = timeout;
        }

        void statistic(long usedTime) {
            if (usedTime < 0) {
                fatal.incrementAndGet();
            } else if (usedTime <= defaultTimeout) {
                success.incrementAndGet();
            }
            total.incrementAndGet();
        }

        /**
         * 99% requests not timeout，reject sate count < 3, then recover state to normal
         */
        boolean isRecover() {
            if (total.get() == 0) {
                return true;
            }

            boolean isRecover = (fatal.get() < 3 && ((double)success.get() / total.get()) >= 0.99);
            reset();
            return isRecover;
        }

        void reset() {
            total.set(0);
            fatal.set(0);
            success.set(0);
        }
    }

    static class RecordRateManager {
        static final RecordRateManager RECORD_RATE_MANAGER = new RecordRateManager();
        // Pari.first = last balance rate
        static final Map<String, Pair<Double, RateLimiter>> RATE_LIMITER_MAP = new ConcurrentHashMap<>();
        static final double MIN_RATE = 0.03D;
        // send a permit in 60 seconds at least
        static final long BASE = MINUTES.toSeconds(1);

        /**
         * Record rate acquire
         */
        boolean acquire(String methodName, double rate) {
            if (rate <= 0) {
                return false;
            }
            Pair<Double, RateLimiter> rateLimiterPair = RATE_LIMITER_MAP.computeIfAbsent(methodName, key -> {
                return Pair.of(rate, RateLimiter.create(rate / BASE));
            });

            int cmp = Double.compare(Optional.ofNullable(rateLimiterPair.getFirst()).orElse(0d), rate);
            if ((cmp < 0 && STATE.get() == NORMAL) || cmp == 1) {
                rateLimiterPair = RATE_LIMITER_MAP.put(methodName, Pair.of(rate, RateLimiter.create(rate / BASE)));
            }

            return rateLimiterPair != null && rateLimiterPair.getSecond().tryAcquire();
        }

        boolean validate() {
            return ConfigManager.INSTANCE.getRecordRate() > 0;
        }

        void changeRate(boolean useMinRate) {
            if (!validate()) {
                return;
            }

            for (Map.Entry<String, Pair<Double, RateLimiter>> entry : RATE_LIMITER_MAP.entrySet()) {
                Pair<Double, RateLimiter> limiterPair = entry.getValue();
                Double targetRate = useMinRate ? MIN_RATE : limiterPair.getFirst();
                RATE_LIMITER_MAP.put(entry.getKey(), Pair.of(limiterPair.getFirst(), RateLimiter.create(targetRate / BASE)));
            }
        }

        /**
         * Decrement record rate by 20%
         * trigger if queue is full
         */
        void decelerate() {
            if (!validate()) {
                return;
            }
            for (Map.Entry<String, Pair<Double, RateLimiter>> entry : RATE_LIMITER_MAP.entrySet()) {
                Pair<Double, RateLimiter> limiterPair = entry.getValue();
                double targetRate = Math.max(limiterPair.getFirst() * 0.8, MIN_RATE);
                if (targetRate > MIN_RATE) {
                    RATE_LIMITER_MAP.put(entry.getKey(), Pair.of(targetRate, RateLimiter.create(targetRate / BASE)));
                } else {
                    break;
                }
            }
        }

        /**
         * Increment record rate to config
         * trigger if queue is empty
         */
        void accelerate() {
            if (!validate()) {
                return;
            }

            for (Map.Entry<String, Pair<Double, RateLimiter>> entry : RATE_LIMITER_MAP.entrySet()) {
                Pair<Double, RateLimiter> limiterPair = entry.getValue();
                Double currentRate = limiterPair.getFirst();
                double targetRate = Math.min(currentRate * 1.2, limiterPair.getSecond().getRate() * BASE);
                int cmp = Double.compare(currentRate, targetRate);
                if (cmp < 0) {
                    RATE_LIMITER_MAP.put(entry.getKey(), Pair.of(limiterPair.getFirst(), RateLimiter.create(targetRate / BASE)));
                } else if (cmp == 0) {
                    STATE.set(NORMAL);
                    break;
                }
            }
        }
    }
}