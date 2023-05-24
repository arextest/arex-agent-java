package io.arex.agent;

import static org.mockito.ArgumentMatchers.any;

import io.arex.agent.bootstrap.AgentInitializer;
import java.lang.instrument.Instrumentation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ArexJavaAgentTest {

    @BeforeAll
    static void beforeAll() {
        Mockito.mockStatic(AgentInitializer.class);
    }

    @AfterAll
    static void afterAll() {
        Mockito.clearAllCaches();
    }

    @Test
    void premain() {
        Instrumentation inst = Mockito.mock(Instrumentation.class);
        Mockito.doNothing().when(inst).appendToBootstrapClassLoaderSearch(any());
        Assertions.assertDoesNotThrow(()-> ArexJavaAgent.premain(null, inst));
    }

    @Test
    void init() {
        Assertions.assertDoesNotThrow(()-> ArexJavaAgent.init(null, null, null));
    }
}
