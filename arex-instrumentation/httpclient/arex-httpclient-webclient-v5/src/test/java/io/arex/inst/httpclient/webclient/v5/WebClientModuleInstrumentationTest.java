package io.arex.inst.httpclient.webclient.v5;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class WebClientModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        assertNotNull(new WebClientModuleInstrumentation().instrumentationTypes());
    }
}