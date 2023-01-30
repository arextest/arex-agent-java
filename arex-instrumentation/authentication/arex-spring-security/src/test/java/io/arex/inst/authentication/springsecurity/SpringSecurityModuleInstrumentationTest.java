package io.arex.inst.authentication.springsecurity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpringSecurityModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        assertNotNull(new SpringSecurityModuleInstrumentation().instrumentationTypes());
    }
}