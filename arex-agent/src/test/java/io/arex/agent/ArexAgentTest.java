package io.arex.agent;

import static org.mockito.ArgumentMatchers.any;

import io.arex.agent.bootstrap.AgentInitializer;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.jar.JarFile;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class ArexAgentTest {

    @BeforeAll
    static void beforeAll() {
        Mockito.mockStatic(AgentInitializer.class);
        Mockito.mockStatic(JarUtils.class);
    }

    @AfterAll
    static void afterAll() {
        Mockito.clearAllCaches();
    }

    @Test
    void premain() {
        try (MockedConstruction<JarFile> mockedConstruction = Mockito.mockConstruction(JarFile.class)) {
            File file = Mockito.mock(File.class);
            Mockito.when(JarUtils.extractNestedBootStrapJar(any())).thenReturn(Arrays.asList(file));
            Instrumentation inst = Mockito.mock(Instrumentation.class);
            Mockito.doNothing().when(inst).appendToBootstrapClassLoaderSearch(any());
            ArexAgent.class.getPackage().getImplementationVersion();
            Assertions.assertDoesNotThrow(()-> ArexAgent.premain(null, inst));
        }
    }

    @Test
    void init() {
        Assertions.assertDoesNotThrow(()-> ArexAgent.init(null, null));
    }
}
