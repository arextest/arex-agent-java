package io.arex.inst.authentication.jcasbin;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JCasbinInstrumentationTest {
    static JCasbinInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new JCasbinInstrumentation();
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
        assertFalse(JCasbinInstrumentation.MethodAdvice.onEnter());
    }

    @Test
    void onExit() {
        assertDoesNotThrow(() -> JCasbinInstrumentation.MethodAdvice.onExit(true, false));
    }
}