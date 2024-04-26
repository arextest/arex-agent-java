package io.arex.agent.bootstrap;

import static io.arex.agent.bootstrap.CreateFileCommon.*;
import static org.mockito.ArgumentMatchers.any;

import java.io.File;
import java.lang.instrument.Instrumentation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;


class AgentInitializerTest {

    static File zipFile = null;
    static File zipExtensionFile = null;
    static Instrumentation instrumentation;

    @BeforeAll
    static void setUp(){
        zipFile = getZipFile();
        zipExtensionFile = getZipExtensionFile();
        instrumentation = Mockito.mock(Instrumentation.class);
    }

    @AfterAll
    static void tearDown() {
        zipFile.deleteOnExit();
        zipExtensionFile.deleteOnExit();
        instrumentation = null;
        CreateFileCommon.clear();
    }

    @Test
    void testFirstInitialize() {
        try (MockedConstruction<AgentClassLoader> mocked = Mockito.mockConstruction(AgentClassLoader.class, (mock, context) -> Mockito.doReturn(InstrumentationInstallerTest.class).when(mock).loadClass(any()))){
            Assertions.assertDoesNotThrow(() -> AgentInitializer.initialize(instrumentation, zipFile, null, AgentInitializerTest.class.getClassLoader()));
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void testDoubleInitialize() {
        try (MockedConstruction<AgentClassLoader> mocked = Mockito.mockConstruction(AgentClassLoader.class, (mock, context) -> Mockito.doReturn(InstrumentationInstallerTest.class).when(mock).loadClass(any()))){
            Assertions.assertDoesNotThrow(() -> AgentInitializer.initialize(instrumentation, zipFile, null, AgentInitializerTest.class.getClassLoader()));
            Assertions.assertDoesNotThrow(() -> AgentInitializer.initialize(instrumentation, zipFile, null, AgentInitializerTest.class.getClassLoader()));
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
