package io.arex.inst.authentication.shiro;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShiroModuleInstrumentationTest {
    ShiroModuleInstrumentation target = new ShiroModuleInstrumentation();

    @Test
    void instrumentationTypes() {
        assertNotNull(target.instrumentationTypes());
    }
}