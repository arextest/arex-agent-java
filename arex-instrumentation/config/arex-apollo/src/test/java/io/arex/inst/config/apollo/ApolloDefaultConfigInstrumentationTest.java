package io.arex.inst.config.apollo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApolloDefaultConfigInstrumentationTest {
    static ApolloDefaultConfigInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new ApolloDefaultConfigInstrumentation();
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
    void onExit() {
        assertDoesNotThrow(ApolloDefaultConfigInstrumentation.UpdateAdvice::onExit);
    }
}