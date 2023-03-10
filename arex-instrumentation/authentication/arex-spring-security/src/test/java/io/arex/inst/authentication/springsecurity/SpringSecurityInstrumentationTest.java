package io.arex.inst.authentication.springsecurity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SpringSecurityInstrumentationTest {
    static SpringSecurityInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new SpringSecurityInstrumentation();
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
        assertFalse(SpringSecurityInstrumentation.PreAuthorizationAdvice.onEnter());
    }

    @Test
    void onExit() {
        AccessDeniedException exception = new AccessDeniedException("mock");
        Object result = null;
        SpringSecurityInstrumentation.PostAuthorizationAdvice.onExit("mock", exception, result);
        assertNotNull(exception);
    }
}