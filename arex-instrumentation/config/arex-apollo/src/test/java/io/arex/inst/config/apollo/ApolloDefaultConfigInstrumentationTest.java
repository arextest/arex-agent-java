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

    @Test
    void testDate() {
        String date1 = "Wed, 19 Jul 2023 08:28:53 GMT";
        String date2 = "Wed, 19 Jul 2023 08:28:54 GMT";
        // compare date1 and date2
//        assertTrue(date1.compareTo(date2) < 0);
        System.out.println(date1.compareTo(date2));

    }
}