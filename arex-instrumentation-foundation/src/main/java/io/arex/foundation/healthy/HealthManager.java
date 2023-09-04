package io.arex.foundation.healthy;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.config.ConfigManager;
import io.arex.agent.bootstrap.internal.Pair;
import io.arex.foundation.model.DecelerateReasonEnum;
import io.arex.foundation.services.TimerService;
import io.arex.inst.runtime.log.LogManager;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.arex.foundation.healthy.HealthManager.RecordRateManager.RECORD_RATE_MANAGER;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * health check rule: <pre>
 * 1. queue overflow:
 * switch to reject state(not record), decrease record rate 20% and reopen record after 30 seconds,
 * then check state after 5 minutes(statistic data during this period),
 * if not recover, continue decrease record rate 20%, check state again after 10 minutes.
 * if recover, switch to normal state, end check and record rate will be recovered in next request(acquire)
 *
 * 2. storage service exception:
 * switch to reject state(not record), reopen record after 10 seconds and check state after 3 minutes
 * statistic data during this period,
 * if not recover, decrease to minimum record rate(about 3 case in 100 min), check state again after 60 minutes.
 * if recover, switch to normal state, end check and record rate will be recovered in next request(acquire)
 * </pre>
 */
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
            new RecordRateChecker(TimeUnit.MILLISECONDS.toNanos(3000));
    private static final RecordRateChecker BALANCE_CHECKER =
            new RecordRateChecker(TimeUnit.MILLISECONDS.toNanos(3000));
    private static ScheduledFuture<?> scheduledFuture = null;

    /**
     * Record rate acquire
     */
    public static boolean acquire(String uri) {
        return RECORD_RATE_MANAGER.acquire(uri, ConfigManager.INSTANCE.getRecordRate());
    }

    public static void onEnqueueRejection() {
        if (STATE.compareAndSet(NORMAL, FAST_REJECT)) {
            LogManager.warn("healthManager.enqueueRejection", "queue overflow! switch to reject state");
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
            LogManager.warn("healthManager.dataServiceRejection", "data service error! switch to reject state");
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
                        STATE.set(NORMAL);
                        scheduledFuture = null;
                    } else {
                        RECORD_RATE_MANAGER.changeRate();
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
            LogManager.warn("healthManager.healthCheckTask", StringUtil.format("check result: state=%s, isRecover=%s",
                    String.valueOf(STATE.get()), String.valueOf(isRecover)));
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
            LogManager.warn("healthManager.isRecover", StringUtil.format("isRecover=%s, fatal=%s, success=%s, total=%s",
                    String.valueOf(isRecover), String.valueOf(fatal), String.valueOf(success), String.valueOf(total)));
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
         * Record rate acquires
         */
        boolean acquire(String methodName, double configRate) {
            if (configRate <= 0) {
                return false;
            }
            Pair<Double, RateLimiter> rateLimiterPair = RATE_LIMITER_MAP.computeIfAbsent(methodName, key -> {
                double targetRate = getCurrentRate(configRate);
                return Pair.of(targetRate, RateLimiter.create(targetRate / BASE));
            });

            updateRate(configRate, rateLimiterPair.getFirst());
            rateLimiterPair = RATE_LIMITER_MAP.get(methodName);
            return rateLimiterPair != null && rateLimiterPair.getSecond().tryAcquire();
        }

        /**
         * custom modify config rate or health check recover :
         * 1. increase or recover (configRate > currentRate && STATE == NORMAL)
         * 2. decrease (configRate < currentRate)
         */
        void updateRate(double configRate, double currentRate) {
            if ((configRate > currentRate && STATE.get() == NORMAL) || configRate < currentRate) {
                for (Map.Entry<String, Pair<Double, RateLimiter>> entry : RATE_LIMITER_MAP.entrySet()) {
                    RATE_LIMITER_MAP.put(entry.getKey(), Pair.of(configRate, RateLimiter.create(configRate / BASE)));
                }
                System.setProperty(ConfigConstants.CURRENT_RATE, String.format("%.2f", configRate));
                System.setProperty(ConfigConstants.DECELERATE_CODE, DecelerateReasonEnum.NORMAL.getCodeStr());
                LogManager.info("healthManager.updateRate",
                        StringUtil.format("update rate, current rate change to: %s", String.format("%.2f", configRate)));
            }
        }

        double getCurrentRate(double rate) {
            if (STATE.get() == NORMAL) {
                return rate;
            }
            // un normally state, use current rate(maybe decelerated)
            String currentRate = System.getProperty(ConfigConstants.CURRENT_RATE);
            if (StringUtil.isNotEmpty(currentRate)) {
                return Double.parseDouble(currentRate);
            }
            return rate;
        }

        boolean validate() {
            return ConfigManager.INSTANCE.getRecordRate() > 0;
        }

        /**
         * Decrement record rate to {@link #MIN_RATE}
         * trigger if service exception
         */
        void changeRate() {
            if (!validate()) {
                return;
            }
            double targetRate = ConfigManager.INSTANCE.getRecordRate();
            for (Map.Entry<String, Pair<Double, RateLimiter>> entry : RATE_LIMITER_MAP.entrySet()) {
                targetRate = MIN_RATE;
                RATE_LIMITER_MAP.put(entry.getKey(), Pair.of(targetRate, RateLimiter.create(targetRate / BASE)));
            }
            System.setProperty(ConfigConstants.CURRENT_RATE, String.format("%.2f", targetRate));
            System.setProperty(ConfigConstants.DECELERATE_CODE, DecelerateReasonEnum.SERVICE_EXCEPTION.getCodeStr());
            LogManager.warn("healthManager.decelerate",
                    StringUtil.format("service exception! decrement record rate, current rate change to: %s",
                            String.format("%.2f", targetRate)));
        }

        /**
         * Decrement record rate by 20%
         * trigger if queue is full
         */
        void decelerate() {
            if (!validate()) {
                return;
            }
            double targetRate = ConfigManager.INSTANCE.getRecordRate();
            for (Map.Entry<String, Pair<Double, RateLimiter>> entry : RATE_LIMITER_MAP.entrySet()) {
                Pair<Double, RateLimiter> limiterPair = entry.getValue();
                targetRate = Math.max(limiterPair.getFirst() * 0.8, MIN_RATE);
                RATE_LIMITER_MAP.put(entry.getKey(), Pair.of(targetRate, RateLimiter.create(targetRate / BASE)));
            }
            System.setProperty(ConfigConstants.CURRENT_RATE, String.format("%.2f", targetRate));
            System.setProperty(ConfigConstants.DECELERATE_CODE, DecelerateReasonEnum.QUEUE_OVERFLOW.getCodeStr());
            LogManager.warn("healthManager.decelerate",
                    StringUtil.format("queue overflow! decrement record rate, current rate change to: %s",
                            String.format("%.2f", targetRate)));
        }

    }
}