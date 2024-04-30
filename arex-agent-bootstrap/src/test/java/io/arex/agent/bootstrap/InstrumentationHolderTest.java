package io.arex.agent.bootstrap;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.instrument.Instrumentation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @since 2024/1/15
 */
@ExtendWith(MockitoExtension.class)
class InstrumentationHolderTest {

    @Mock
    Instrumentation instrumentation;
    @Mock
    ClassLoader agentClassLoader;

    @Mock
    File agentFile;

    @Test
    void getInstrumentation() {
        InstrumentationHolder.setInstrumentation(instrumentation);
        assertEquals(instrumentation, InstrumentationHolder.getInstrumentation());
    }

    @Test
    void getAgentClassLoader() {
        InstrumentationHolder.setAgentClassLoader(agentClassLoader);
        assertEquals(agentClassLoader, InstrumentationHolder.getAgentClassLoader());
    }

    @Test
    void getAgentFile() {
        InstrumentationHolder.setAgentFile(agentFile);
        assertEquals(agentFile, InstrumentationHolder.getAgentFile());
    }
}
