package io.arex.inst.runtime.util;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.service.DataCollector;
import io.arex.inst.runtime.service.DataService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MockUtilsTest {
    static ConfigBuilder configBuilder = null;
    static DataCollector dataCollector;
    @BeforeAll
    static void setUp() {
        configBuilder = ConfigBuilder.create("test");
        dataCollector = Mockito.mock(DataCollector.class);
        DataService.builder().setDataCollector(dataCollector).build();
    }

    @AfterAll
    static void tearDown() {
        configBuilder = null;
        Mockito.clearAllCaches();
    }

    @Test
    void recordMocker() {
        configBuilder.enableDebug(true);
        configBuilder.build();
        ArexMocker dynamicClass = MockUtils.createDynamicClass("test", "test");
        Assertions.assertDoesNotThrow(() -> MockUtils.recordMocker(dynamicClass));
    }

    @Test
    void replayMocker() {
        configBuilder.enableDebug(true);
        configBuilder.build();
        ArexMocker dynamicClass = MockUtils.createDynamicClass("test", "test");
        assertNull(MockUtils.replayMocker(dynamicClass));

        // return response
        configBuilder.enableDebug(false);
        configBuilder.build();
        Mockito.when(dataCollector.query(Mockito.any(), Mockito.any())).thenReturn("test");
        ArexMocker dynamicClass2 = MockUtils.createDynamicClass("test", "test");
        assertNull(MockUtils.replayMocker(dynamicClass2));
    }

    @Test
    void checkResponse() {
        configBuilder.build();
        // null
        assertFalse(MockUtils.checkResponseMocker(null));

        // null response
        final ArexMocker mocker = new ArexMocker();
        assertFalse(MockUtils.checkResponseMocker(mocker));

        // empty body
        ArexMocker dynamicClass = MockUtils.createDynamicClass("test", "test");
        assertFalse(MockUtils.checkResponseMocker(dynamicClass));

        // empty type
        dynamicClass.getTargetResponse().setBody("test");
        assertFalse(MockUtils.checkResponseMocker(dynamicClass));

        // normal mocker
        dynamicClass.getTargetResponse().setType("java.lang.String");
        assertTrue(MockUtils.checkResponseMocker(dynamicClass));
    }
}