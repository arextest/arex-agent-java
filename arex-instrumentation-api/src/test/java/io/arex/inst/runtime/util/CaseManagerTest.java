package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.EventProcessorTest.TestJacksonSerializable;
import io.arex.inst.runtime.serializer.Serializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class CaseManagerTest {
    static MockedStatic<ContextManager> contextManagerMocked;

    @BeforeAll
    static void setUp() {
        Serializer.builder(new TestJacksonSerializable()).build();
        contextManagerMocked = Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        contextManagerMocked = null;
        Mockito.clearAllCaches();
    }

    @Test
    void invalid() {
        // empty recordId
        Assertions.assertDoesNotThrow(() -> CaseManager.invalid(null, "testOperationName"));
        Assertions.assertTrue(CaseManager.isInvalidCase(null));

        final ArexContext context = ArexContext.of("testRecordId");
        Mockito.when(ContextManager.getRecordContext("testRecordId")).thenReturn(context);
        Assertions.assertFalse(context.isInvalidCase());
        Assertions.assertFalse(CaseManager.isInvalidCase("testRecordId"));

        CaseManager.invalid("testRecordId", "testOperationName");
        Assertions.assertTrue(context.isInvalidCase());
        Assertions.assertTrue(CaseManager.isInvalidCase("testRecordId"));

        // test invalid case with null context
        Mockito.when(ContextManager.getRecordContext("testRecordId")).thenReturn(null);
        Assertions.assertFalse(CaseManager.isInvalidCase("testRecordId"));
        Assertions.assertDoesNotThrow(() -> CaseManager.invalid("testRecordId", "testOperationName"));

        // test invalid case with exception
        Mockito.when(ContextManager.getRecordContext("testRecordId")).thenThrow(new RuntimeException("test exception"));
        Assertions.assertDoesNotThrow(() -> CaseManager.invalid("testRecordId", "testOperationName"));
    }
}