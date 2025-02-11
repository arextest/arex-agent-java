package io.arex.inst.runtime.util;

import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.EventProcessorTest.TestJacksonSerializable;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.service.DataCollector;
import io.arex.inst.runtime.service.DataService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

class CaseManagerTest {
    static MockedStatic<ContextManager> contextManagerMocked;
    static DataCollector dataCollector;

    @BeforeAll
    static void setUp() {
        Serializer.builder(new TestJacksonSerializable()).build();
        contextManagerMocked = Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
        dataCollector = Mockito.mock(DataCollector.class);
        DataService.setDataCollector(Collections.singletonList(dataCollector));
    }

    @AfterAll
    static void tearDown() {
        contextManagerMocked = null;
        dataCollector = null;
        DataService.setDataCollector(null);
        Mockito.clearAllCaches();
    }

    @Test
    void invalid() {
        // empty recordId
        Assertions.assertDoesNotThrow(() -> CaseManager.invalid(null, null, "testOperationName", "queueOverflow"));
        Assertions.assertTrue(CaseManager.isInvalidCase(null));

        ArexContext context = ArexContext.of("testRecordId");
        Mockito.when(ContextManager.getContext("testRecordId")).thenReturn(context);
        Assertions.assertFalse(context.isInvalidCase());
        Assertions.assertFalse(CaseManager.isInvalidCase("testRecordId"));
        System.setProperty("arex.service.name", "testServiceName");
        CaseManager.invalid("testRecordId", null, "testOperationName", "queueOverflow");
        Assertions.assertTrue(context.isInvalidCase());
        Assertions.assertTrue(CaseManager.isInvalidCase("testRecordId"));
        Mockito.verify(dataCollector, Mockito.times(1)).invalidCase(Mockito.anyString());

        // replayId
        CaseManager.invalid("testRecordId", "testReplayId", "testOperationName", "queueOverflow");
        context = ArexContext.of("testRecordId", "testReplayId");
        Mockito.when(ContextManager.getContext("testReplayId")).thenReturn(context);
        Assertions.assertFalse(context.isInvalidCase());
        Assertions.assertFalse(CaseManager.isInvalidCase("testReplayId"));

        // test invalid case with null context
        Mockito.when(ContextManager.getContext("testRecordId")).thenReturn(null);
        Assertions.assertFalse(CaseManager.isInvalidCase("testRecordId"));
        Assertions.assertDoesNotThrow(() -> CaseManager.invalid("testRecordId", null, "testOperationName", "queueOverflow"));

        // test invalid case with exception
        Mockito.when(ContextManager.getContext("testRecordId")).thenThrow(new RuntimeException("test exception"));
        Assertions.assertDoesNotThrow(() -> CaseManager.invalid("testRecordId", null, "testOperationName", "queueOverflow"));
    }
}
