package io.arex.inst.loader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InjectClassInstrumentationTest {

    static InjectClassInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new InjectClassInstrumentation();
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
        assertNull(InjectClassInstrumentation.LoadClassAdvice.onEnter(null, null));
    }

    @Test
    void onExit() {
        assertDoesNotThrow(() -> InjectClassInstrumentation.LoadClassAdvice.onExit(null, String.class));
    }
}