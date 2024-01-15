package io.arex.inst.runtime.util.sizeof;

import io.arex.agent.bootstrap.InstrumentationHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.lang.instrument.Instrumentation;

import static org.junit.jupiter.api.Assertions.*;

class AgentSizeOfTest {

    static AgentSizeOf caller;
    static Instrumentation instrumentation;

    @BeforeAll
    static void setUp() {
        caller = AgentSizeOf.newInstance();
        Mockito.mockStatic(InstrumentationHolder.class);
        instrumentation = Mockito.mock(Instrumentation.class);
    }

    @AfterAll
    static void tearDown() {
        caller = null;
        instrumentation = null;
        Mockito.clearAllCaches();
    }

    @Test
    void deepSizeOf() {
        assertEquals(caller.deepSizeOf("mock"), 0);
    }

    @Test
    void sizeOf() {
        assertEquals(caller.sizeOf("mock"), 0);
        Mockito.when(InstrumentationHolder.getInstrumentation()).thenReturn(instrumentation);
        caller = AgentSizeOf.newInstance();
        assertEquals(caller.sizeOf("mock"), 0);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1 bytes",
            "1024, 1 KB",
            "1048576, 1 MB",
            "1073741824, 1 GB"
    })
    void humanReadableUnits(long bytes, String expected) {
        assertEquals(AgentSizeOf.humanReadableUnits(bytes), expected);
    }

    @Test
    void checkMemorySizeLimit() {
        assertTrue(caller.checkMemorySizeLimit("mock", 1));
    }
}