package io.arex.inst.httpclient.resttemplate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RestTemplateModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        RestTemplateModuleInstrumentation restTemplateModuleInstrumentation = new RestTemplateModuleInstrumentation();
        assertEquals(1, restTemplateModuleInstrumentation.instrumentationTypes().size());
    }
}