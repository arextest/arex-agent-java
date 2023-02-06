package io.arex.inst.httpservlet.inst;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServletModuleInstrumentationV3Test {
    ServletModuleInstrumentationV3 module = new ServletModuleInstrumentationV3();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void instrumentationTypes() {
        assertEquals(2, module.instrumentationTypes().size());
    }
}
