package io.arex.inst.executors;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExecutorsModuleInstrumentationTest {
    ExecutorsModuleInstrumentation module = new ExecutorsModuleInstrumentation();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void instrumentationTypes() {
        assertEquals(4, module.instrumentationTypes().size());
    }
}
