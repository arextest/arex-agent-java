package io.arex.foundation.healthy;

import io.arex.foundation.config.ConfigManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class HealthManagerTest {

    @ParameterizedTest
    @CsvSource({
            "0, false",
            "1, true",
            "2, false"
    })
    void acquire(String rate, boolean expect) {
        System.out.println("rate="+rate+", expect="+expect);
        ConfigManager.INSTANCE.setRecordRate(rate);
        boolean result = HealthManager.acquire("mock");
        assertEquals(expect, result);
    }
}