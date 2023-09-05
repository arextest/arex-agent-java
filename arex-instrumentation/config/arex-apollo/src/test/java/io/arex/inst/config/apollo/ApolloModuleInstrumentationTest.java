package io.arex.inst.config.apollo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApolloModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        assertNotNull(new ApolloModuleInstrumentation().instrumentationTypes());
    }
}