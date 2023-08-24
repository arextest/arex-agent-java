package io.arex.foundation.healthy;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.foundation.config.ConfigManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class HealthManagerTest {

    @BeforeEach
    void setUp() {
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
        HealthManager.RecordRateManager.RECORD_RATE_MANAGER.changeRate(false);
        assertNull(System.getProperty(ConfigConstants.CURRENT_RATE));
        ConfigManager.INSTANCE.setRecordRate(1);
        HealthManager.acquire("mock");
        HealthManager.RecordRateManager.RECORD_RATE_MANAGER.changeRate(true);
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
}
