package io.arex.inst.authentication.shiro;

import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

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
        ShiroHttpServletRequest request = Mockito.mock(ShiroHttpServletRequest.class);
        Mockito.when(request.getHeader(any())).thenReturn("mock");
        assertTrue(ShiroInstrumentation.PreHandleAdvice.onEnter(request));
    }

    @Test
    void onExit() {
        assertDoesNotThrow(() -> ShiroInstrumentation.PreHandleAdvice.onExit(true, false));
    }
}