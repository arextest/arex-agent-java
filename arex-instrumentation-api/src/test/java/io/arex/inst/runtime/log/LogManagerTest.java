package io.arex.inst.runtime.log;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

class LogManagerTest {

    @Test
    void addTag() {
        Assertions.assertDoesNotThrow(() -> LogManager.addTag("testRecordId", "testReplayId"));
        Assertions.assertDoesNotThrow(() -> LogManager.addTag("", ""));
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