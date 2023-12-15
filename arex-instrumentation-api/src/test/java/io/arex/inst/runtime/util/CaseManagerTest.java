package io.arex.inst.runtime.util;

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

class CaseManagerTest {
    static MockedStatic<ContextManager> contextManagerMocked;
    static DataCollector dataCollector;

    @BeforeAll
    static void setUp() {
        Serializer.builder(new TestJacksonSerializable()).build();
        contextManagerMocked = Mockito.mockStatic(ContextManager.class);
        dataCollector = Mockito.mock(DataCollector.class);
        DataService.builder().setDataCollector(dataCollector).build();
    }

    @AfterAll
    static void tearDown() {
        contextManagerMocked = null;
        dataCollector = null;
        DataService.builder().setDataCollector(null).build();
        Mockito.clearAllCaches();
    }

    @Test
    void invalid() {
        // empty recordId
        Assertions.assertDoesNotThrow(() -> CaseManager.invalid(null, null, "testOperationName", "queueOverflow"));
        Assertions.assertTrue(CaseManager.isInvalidCase(null));

        final ArexContext context = ArexContext.of("testRecordId");
        Mockito.when(ContextManager.getContext("testRecordId")).thenReturn(context);
        Assertions.assertFalse(context.isInvalidCase());
        Assertions.assertFalse(CaseManager.isInvalidCase("testRecordId"));
        System.setProperty("arex.service.name", "testServiceName");
        CaseManager.invalid("testRecordId", null, "testOperationName", "queueOverflow");
        Assertions.assertTrue(context.isInvalidCase());
        Assertions.assertTrue(CaseManager.isInvalidCase("testRecordId"));
        Mockito.verify(dataCollector, Mockito.times(1)).invalidCase(Mockito.anyString());

        // test invalid case with null context
        Mockito.when(ContextManager.getContext("testRecordId")).thenReturn(null);
        Assertions.assertFalse(CaseManager.isInvalidCase("testRecordId"));
        Assertions.assertDoesNotThrow(() -> CaseManager.invalid("testRecordId", null, "testOperationName", "queueOverflow"));

        // test invalid case with exception
        Mockito.when(ContextManager.getContext("testRecordId")).thenThrow(new RuntimeException("test exception"));
        Assertions.assertDoesNotThrow(() -> CaseManager.invalid("testRecordId", null, "testOperationName", "queueOverflow"));
    }
}
