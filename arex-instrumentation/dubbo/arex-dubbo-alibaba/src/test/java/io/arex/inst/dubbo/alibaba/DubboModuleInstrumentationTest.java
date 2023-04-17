package io.arex.inst.dubbo.alibaba;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DubboModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        assertNotNull(new DubboModuleInstrumentation().instrumentationTypes());
    }
}