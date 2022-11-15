package io.arex.inst.dynamic;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.services.ConfigService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DynamicClassModuleInstrumentationTest {
    static DynamicClassModuleInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new DynamicClassModuleInstrumentation();
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @Test
    void instrumentationTypes() {
        ConfigService.DynamicClassConfiguration dynamicClassConfiguration = new ConfigService.DynamicClassConfiguration();
        dynamicClassConfiguration.setFullClassName("test");
        ConfigManager.INSTANCE.setDynamicClassList(Collections.singletonList(dynamicClassConfiguration));
        assertNotNull(target.instrumentationTypes());
    }
}