package io.arex.inst.dubbo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DubboModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        assertNotNull(new DubboModuleInstrumentation().instrumentationTypes());
    }
}