package io.arex.agent.bootstrap;

import static io.arex.agent.bootstrap.CreateFileCommon.*;
import static org.mockito.ArgumentMatchers.any;

import java.io.File;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;


class AgentInitializerTest {

    static File zipFile = null;
    static File zipExtensionFile = null;


    @BeforeAll
    static void setUp(){
        zipFile = getZipFile();
        zipExtensionFile = getZipExtensionFile();
    }

    @AfterAll
    static void tearDown() {
        zipFile.deleteOnExit();
        zipExtensionFile.deleteOnExit();
        CreateFileCommon.clear();
    }

    @Test
    void testFirstInitialize() {
        try (MockedConstruction<AgentClassLoader> mocked = Mockito.mockConstruction(AgentClassLoader.class, (mock, context) -> Mockito.doReturn(InstrumentationInstallerTest.class).when(mock).loadClass(any()))){
            Assertions.assertDoesNotThrow(() -> AgentInitializer.initialize(ByteBuddyAgent.install(), zipFile, null));
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void testDoubleInitialize() {
        try (MockedConstruction<AgentClassLoader> mocked = Mockito.mockConstruction(AgentClassLoader.class, (mock, context) -> Mockito.doReturn(InstrumentationInstallerTest.class).when(mock).loadClass(any()))){
            Assertions.assertDoesNotThrow(() -> AgentInitializer.initialize(ByteBuddyAgent.install(), zipFile, null));
            Assertions.assertDoesNotThrow(() -> AgentInitializer.initialize(ByteBuddyAgent.install(), zipFile, null));
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
