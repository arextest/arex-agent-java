package io.arex.inst.jwt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JWTModuleInstrumentationTest {
    JWTModuleInstrumentation target = new JWTModuleInstrumentation();

    @Test
    void instrumentationTypes() {
        assertNotNull(target.instrumentationTypes());
    }
}