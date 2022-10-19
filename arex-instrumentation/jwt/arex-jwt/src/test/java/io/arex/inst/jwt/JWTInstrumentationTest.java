package io.arex.inst.jwt;

import io.arex.foundation.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class JWTInstrumentationTest {
    static JWTInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new JWTInstrumentation();
        Mockito.mockStatic(ContextManager.class);
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
        target.methodAdvices();
    }

    @Test
    void onEnter() {
        JWTInstrumentation.MethodAdvice.onEnter();
    }

    @Test
    void onExit() {
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        JWTInstrumentation.MethodAdvice.onExit("jwt", null);
    }
}