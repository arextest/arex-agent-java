package io.arex.inst.dubbo.alibaba;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DubboFilterInstrumentationTest {
    static DubboFilterInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new DubboFilterInstrumentation();
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @Test
    void methodAdvices() {
        assertNotNull(target.methodAdvices());
    }

    @Test
    void onEnter() {
        assertDoesNotThrow(() -> DubboFilterInstrumentation.InvokeAdvice.onExit(null, null));
    }
}