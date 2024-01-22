package io.arex.inst.authentication.shiro;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShiroDelegatingSubjectInstrumentationTest {
    static ShiroDelegatingSubjectInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new ShiroDelegatingSubjectInstrumentation();
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
        assertFalse(ShiroDelegatingSubjectInstrumentation.AssertAuthzCheckPossibleAdvice.onEnter());
    }
}