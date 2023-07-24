package io.arex.inst.runtime.log;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

class LogManagerTest {
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        ArexContext context = Mockito.mock(ArexContext.class);
        Mockito.when(ContextManager.currentContext()).thenReturn(context);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void buildTitle() {
        assertEquals("[[title=arex.test]]", LogManager.buildTitle("test"));
    }

    @Test
    void testBuildTitle() {
        assertEquals("[[title=arex.pretest]]", LogManager.buildTitle("pre", "test"));
    }

    @Test
    void testInfoAndWarn() {
        // no extension log
        Assertions.assertDoesNotThrow(() -> LogManager.info("test", "test"));
        Assertions.assertDoesNotThrow(() -> LogManager.warn("test", "test"));
        Assertions.assertDoesNotThrow(() -> LogManager.warn("test", new Throwable()));
        // extension log
        LogManager.build(Collections.singletonList(Mockito.mock(Logger.class)));
        Assertions.assertDoesNotThrow(() -> LogManager.info("test", "test"));
        Assertions.assertDoesNotThrow(() -> LogManager.warn("test", "test"));
        Assertions.assertDoesNotThrow(() -> LogManager.warn("test", new Throwable()));
    }


    @Test
    void setContextMap() {
        Assertions.assertDoesNotThrow(() -> LogManager.setContextMap(Collections.emptyMap()));
        Assertions.assertDoesNotThrow(() -> LogManager.setContextMap(Collections.singletonMap("test", "test")));
    }
}