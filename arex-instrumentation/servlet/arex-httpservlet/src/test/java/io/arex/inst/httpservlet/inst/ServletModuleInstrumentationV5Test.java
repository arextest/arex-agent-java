package io.arex.inst.httpservlet.inst;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServletModuleInstrumentationV5Test {

    ServletModuleInstrumentationV5 module = new ServletModuleInstrumentationV5();

    @Test
    void instrumentationTypes() {
        assertEquals(2, module.instrumentationTypes().size());
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }
}
