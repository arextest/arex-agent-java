package io.arex.foundation.healthy;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.Assert;
import io.arex.agent.bootstrap.util.ReflectUtil;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.services.TimerService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;

class HealthManagerTest {

    static AtomicInteger STATE = new AtomicInteger(HealthManager.NORMAL);
    static MockedStatic<TimerService> timerServiceMocker;
    static HealthManager.HealthCheckTask healthCheckTask;

    @BeforeAll
    static void setUp() throws Exception {
        Object stateObj = ReflectUtil.getFieldOrInvokeMethod(() -> HealthManager.class.getDeclaredField("STATE"),null);
        if (stateObj instanceof AtomicInteger) {
            STATE = (AtomicInteger) stateObj;
        }
        timerServiceMocker = Mockito.mockStatic(TimerService.class);
        healthCheckTask = new HealthManager.HealthCheckTask();
    }

    @AfterAll
    static void tearDown() {
        timerServiceMocker = null;
        healthCheckTask = null;
        Mockito.clearAllCaches();
    }

    @BeforeEach
    void before() {
        STATE.set(HealthManager.NORMAL);
        System.clearProperty(ConfigConstants.CURRENT_RATE);
    }

    @ParameterizedTest
    @CsvSource({
            "0, false",
            "1, true",
            "2, true"
    })
    void acquire(int rate, boolean expect) {
        ConfigManager.INSTANCE.setRecordRate(rate);
        boolean result = HealthManager.acquire("mock");
        assertEquals(expect, result);
    }

    @Test
    void changeRate() {
        ConfigManager.INSTANCE.setRecordRate(0);
        HealthManager.RecordRateManager.RECORD_RATE_MANAGER.changeRate();
        assertNull(System.getProperty(ConfigConstants.CURRENT_RATE));
        ConfigManager.INSTANCE.setRecordRate(1);
        HealthManager.acquire("mock");
        HealthManager.RecordRateManager.RECORD_RATE_MANAGER.changeRate();
        assertEquals(String.valueOf(HealthManager.RecordRateManager.MIN_RATE),
                System.getProperty(ConfigConstants.CURRENT_RATE));
    }

    @Test
    void decelerate() {
        ConfigManager.INSTANCE.setRecordRate(0);
        HealthManager.RecordRateManager.RECORD_RATE_MANAGER.decelerate();
        assertNull(System.getProperty(ConfigConstants.CURRENT_RATE));
        ConfigManager.INSTANCE.setRecordRate(1);
        HealthManager.acquire("mock");
        HealthManager.RecordRateManager.RECORD_RATE_MANAGER.decelerate();
        assertEquals("0.80", System.getProperty(ConfigConstants.CURRENT_RATE));
    }

    @Test
    void onEnqueueRejection() {
        HealthManager.onEnqueueRejection();
        timerServiceMocker.verify(() -> TimerService.schedule(any(), anyLong(), any()));
    }

    @Test
    void onDataServiceRejection() {
        HealthManager.onDataServiceRejection();
        timerServiceMocker.verify(() -> TimerService.schedule(any(), anyLong(), any()), atLeastOnce());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1, 1",
            "1, 0, 0",
            "2, 1, 1"
    })
    void getCurrentRate(int state, double rate, double expect) {
        STATE.set(state);
        System.setProperty(ConfigConstants.CURRENT_RATE, rate == 0 ? "" : String.valueOf(rate));
        double result = HealthManager.RecordRateManager.RECORD_RATE_MANAGER.getCurrentRate(rate);
        assertEquals(expect, result);
    }

    @ParameterizedTest
    @MethodSource("runCase")
    void run(Runnable mocker, Assert asserts) {
        mocker.run();
        healthCheckTask.run();
        asserts.verity();
    }

    static Stream<Arguments> runCase() {
        Runnable serviceExceptionMocker1 = () -> {
            STATE.set(HealthManager.SERVICE_EXCEPTION);
        };
        Runnable serviceExceptionMocker2 = () -> {
            serviceExceptionMocker1.run();
            HealthManager.reportUsedTime(-1, false);
        };
        Runnable queueOverflowMocker1 = () -> {
            STATE.set(HealthManager.QUEUE_OVERFLOW);
        };
        Runnable queueOverflowMocker2 = () -> {
            queueOverflowMocker1.run();
            HealthManager.reportUsedTime(-1, true);
        };
        Assert rateIsNull = () -> {
            assertNull(System.getProperty(ConfigConstants.CURRENT_RATE));
        };
        Assert rateNotNull = () -> {
            assertNotNull(System.getProperty(ConfigConstants.CURRENT_RATE));
        };
        return Stream.of(
                arguments(serviceExceptionMocker1, rateIsNull),
                arguments(serviceExceptionMocker2, rateNotNull),
                arguments(queueOverflowMocker1, rateIsNull),
                arguments(queueOverflowMocker2, rateNotNull)
        );
    }
}
