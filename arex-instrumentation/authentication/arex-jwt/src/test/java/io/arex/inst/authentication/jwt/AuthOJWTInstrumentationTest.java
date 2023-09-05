package io.arex.inst.authentication.jwt;

import io.arex.inst.runtime.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class AuthOJWTInstrumentationTest {
    static AuthOJWTInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new AuthOJWTInstrumentation();
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
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
        assertFalse(AuthOJWTInstrumentation.MethodAdvice.onEnter());
    }

    @Test
    void onExit() {
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        assertDoesNotThrow(() -> AuthOJWTInstrumentation.MethodAdvice.onExit("jwt", null));
    }
}