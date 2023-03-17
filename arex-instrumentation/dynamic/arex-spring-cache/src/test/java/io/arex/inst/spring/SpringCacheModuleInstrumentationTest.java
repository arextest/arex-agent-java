package io.arex.inst.spring;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SpringCacheModuleInstrumentationTest {
    @Test
    void testInstrumentationTypes() {
        assertEquals(1, new SpringCacheModuleInstrumentation().instrumentationTypes().size());
    }

}