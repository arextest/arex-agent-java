package io.arex.inst.dynamic;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DynamicClassModuleInstrumentationTest {
    static DynamicClassModuleInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new DynamicClassModuleInstrumentation();
        System.setProperty("arex.dynamic.class", "java.lang.System#lineSeparator#null");
    }

    @AfterAll
    static void tearDown() {
        target = null;
        System.clearProperty("arex.dynamic.class");
    }

    @Test
    void instrumentationTypes() {
        target.instrumentationTypes();
    }
}