package io.arex.inst.time;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.runtime.config.ConfigBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TimeMachineModuleInstrumentationTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void instrumentationTypes() {
        TimeMachineModuleInstrumentation inst = new TimeMachineModuleInstrumentation();
        assertEquals(4, inst.instrumentationTypes().size());
    }
}
