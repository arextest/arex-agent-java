package io.arex.inst.httpservlet.handler;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServletV5RequestHandlerTest {

    static ServletV5RequestHandler target;

    @BeforeAll
    static void setUp() {
        target = new ServletV5RequestHandler();
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @Test
    void name() {
        assertNotNull(target.name());
    }

    @Test
    void preHandle() {
        assertDoesNotThrow(() ->  target.preHandle(null));
    }

    @Test
    void handleAfterCreateContext() {
        assertDoesNotThrow(() ->  target.handleAfterCreateContext(null));
    }

    @Test
    void postHandle() {
        assertDoesNotThrow(() ->  target.postHandle(null, null));
    }
}