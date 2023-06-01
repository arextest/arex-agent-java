package io.arex.inst.dubbo.apache.v3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DubboModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        assertNotNull(new DubboModuleInstrumentation().instrumentationTypes());
    }
}