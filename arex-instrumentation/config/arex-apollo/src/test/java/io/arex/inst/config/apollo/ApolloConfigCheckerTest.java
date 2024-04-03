package io.arex.inst.config.apollo;

import io.arex.inst.runtime.config.Config;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ApolloConfigCheckerTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
        Mockito.when(Config.get().getString(any(), any())).thenReturn("mock");
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void unloadApollo() {
        assertFalse(ApolloConfigChecker.unloadApollo());
    }
}