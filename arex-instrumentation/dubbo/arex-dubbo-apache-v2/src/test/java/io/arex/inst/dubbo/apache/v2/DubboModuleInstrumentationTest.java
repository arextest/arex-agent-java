package io.arex.inst.dubbo.apache.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DubboModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        assertNotNull(new DubboModuleInstrumentation().instrumentationTypes());
    }
}