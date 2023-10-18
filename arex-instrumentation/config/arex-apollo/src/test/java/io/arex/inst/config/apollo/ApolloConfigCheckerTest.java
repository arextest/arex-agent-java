package io.arex.inst.config.apollo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApolloConfigCheckerTest {

    @Test
    void unloadApollo() {
        assertFalse(ApolloConfigChecker.unloadApollo());
    }
}