package io.arex.inst.lettuce.v6;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LettuceModuleInstrumentationTest {
    LettuceModuleInstrumentation instrumentation;

    @BeforeEach
    void setUp() {
        instrumentation = new LettuceModuleInstrumentation();
    }

    @AfterEach
    void tearDown() {
        instrumentation = null;
    }

    @Test
    void instrumentationTypes() {
        assert instrumentation.instrumentationTypes().size() == 2;
    }
}
