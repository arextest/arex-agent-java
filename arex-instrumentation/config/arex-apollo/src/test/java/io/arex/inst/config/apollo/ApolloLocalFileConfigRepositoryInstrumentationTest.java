package io.arex.inst.config.apollo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApolloLocalFileConfigRepositoryInstrumentationTest {

    static ApolloLocalFileConfigRepositoryInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new ApolloLocalFileConfigRepositoryInstrumentation();
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
        assertDoesNotThrow(ApolloLocalFileConfigRepositoryInstrumentation.PersistAdvice::onEnter);
    }
}