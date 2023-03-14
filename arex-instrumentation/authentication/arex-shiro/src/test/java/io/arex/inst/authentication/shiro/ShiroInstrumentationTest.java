package io.arex.inst.authentication.shiro;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ShiroInstrumentationTest {
    static ShiroInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new ShiroInstrumentation();
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
        assertFalse(ShiroInstrumentation.PreHandleAdvice.onEnter());
    }

    @Test
    void onExit() {
        assertDoesNotThrow(() -> ShiroInstrumentation.PreHandleAdvice.onExit(true, false));
    }
}