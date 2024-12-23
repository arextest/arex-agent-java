package io.arex.inst.httpclient;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class AsyncHttpClientModuleInstrumentationTest {
    private static AsyncHttpClientModuleInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new AsyncHttpClientModuleInstrumentation();
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void instrumentationTypes() {
        assertEquals(1, target.instrumentationTypes().size());
    }
}
