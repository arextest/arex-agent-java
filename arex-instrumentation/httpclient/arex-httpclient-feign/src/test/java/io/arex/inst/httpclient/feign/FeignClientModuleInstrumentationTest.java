package io.arex.inst.httpclient.feign;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FeignClientModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        assertEquals(1, new FeignClientModuleInstrumentation().instrumentationTypes().size());
    }

}
