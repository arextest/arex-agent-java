package io.arex.inst.dubbo.alibaba;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DubboCodecInstrumentationTest {
    static DubboCodecInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new DubboCodecInstrumentation();
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
        assertFalse(DubboCodecInstrumentation.InvokeAdvice.onEnter(null, null, null));
    }
}