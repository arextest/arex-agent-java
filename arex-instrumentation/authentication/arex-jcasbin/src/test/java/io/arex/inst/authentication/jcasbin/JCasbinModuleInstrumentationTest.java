package io.arex.inst.authentication.jcasbin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JCasbinModuleInstrumentationTest {

    JCasbinModuleInstrumentation target = new JCasbinModuleInstrumentation();

    @Test
    void instrumentationTypes() {
        assertNotNull(target.instrumentationTypes());
    }
}