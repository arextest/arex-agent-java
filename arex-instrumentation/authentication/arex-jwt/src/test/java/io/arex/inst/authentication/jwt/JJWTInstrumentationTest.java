package io.arex.inst.authentication.jwt;

import io.jsonwebtoken.Jwt;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class JJWTInstrumentationTest {
    static JJWTInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new JJWTInstrumentation();
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
        assertNull(JJWTInstrumentation.MethodAdvice.onEnter(null));
    }

    @Test
    void onExit() {
        Jwt mockJwt = Mockito.mock(Jwt.class);
        assertDoesNotThrow(() -> JJWTInstrumentation.MethodAdvice.onExit(mockJwt, null));
    }
}