package io.arex.inst.runtime.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.EventProcessorTest.TestGsonSerializer;
import io.arex.inst.runtime.listener.EventProcessorTest.TestJacksonSerializable;
import io.arex.inst.runtime.match.ReplayMatcher;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.service.DataCollector;
import io.arex.inst.runtime.service.DataService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(CaseManager.class);

        configBuilder = ConfigBuilder.create("test");
        dataCollector = Mockito.mock(DataCollector.class);
        DataService.setDataCollector(Collections.singletonList(dataCollector));

        final List<StringSerializable> list = new ArrayList<>(2);
        list.add(new TestJacksonSerializable());
        list.add(new TestGsonSerializer());
        Serializer.builder(list).build();
        Mockito.mockStatic(MergeRecordUtil.class);
        Mockito.mockStatic(ReplayMatcher.class);
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

        // invalid case
        Mockito.when(CaseManager.isInvalidCase(any())).thenReturn(true);
        Assertions.assertDoesNotThrow(() -> MockUtils.recordMocker(dynamicClass));

        // merge case
        Mockito.when(CaseManager.isInvalidCase(any())).thenReturn(false);
        ArexMocker servletMocker = MockUtils.createServlet("mock");
        servletMocker.setNeedMerge(true);
        Assertions.assertDoesNotThrow(() -> MockUtils.recordMocker(servletMocker));

        // remain case
        servletMocker.setNeedMerge(false);
        Assertions.assertDoesNotThrow(() -> MockUtils.recordMocker(servletMocker));
    }

    @Test
    void replayMocker() {
        configBuilder.enableDebug(true);
        configBuilder.build();
        ArexMocker dynamicClass = MockUtils.createDynamicClass("test", "test");
        assertNull(MockUtils.replayBody(dynamicClass));
        assertNull(MockUtils.replayMocker(dynamicClass));

        // return response
        configBuilder.enableDebug(false);
        configBuilder.build();
        String responseJson = "{\"id\":\"64ec180f7071c91a03cde866\",\"categoryType\":{\"name\":\"DynamicClass\",\"entryPoint\":false,\"skipComparison\":true},\"replayId\":null,\"recordId\":\"AREX-10-4-202-26-46993323299502\",\"appId\":\"arex-test-app\",\"recordEnvironment\":0,\"creationTime\":1693194255518,\"updateTime\":0,\"expirationTime\":1693539855663,\"targetRequest\":{\"body\":null,\"attributes\":null,\"type\":null},\"targetResponse\":{\"body\":\"1693194255518\",\"attributes\":null,\"type\":\"java.lang.Long\"},\"operationName\":\"java.lang.System.currentTimeMillis\",\"recordVersion\":\"0.3.8\"}";
        Mockito.when(dataCollector.query(anyString(), any())).thenReturn(responseJson);
        Mockito.when(CaseManager.isInvalidCase("mock-replay-id")).thenReturn(false);
        Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock-trace-id", "mock-replay-id"));
        dynamicClass = MockUtils.createDynamicClass("test", "test");
        Object actualResult = MockUtils.replayBody(dynamicClass);
        assertEquals(1693194255518L, actualResult);

        // invalid case
        Mockito.when(CaseManager.isInvalidCase("mock-replay-id")).thenReturn(true);
        assertNull(MockUtils.replayBody(dynamicClass));

        // null replayId but is not config file
        Mockito.when(CaseManager.isInvalidCase(null)).thenReturn(true);
        Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock-trace-id", null));
        assertNull(MockUtils.replayBody(dynamicClass));

        // null replayId and is config file
        ArexMocker configFile = MockUtils.createConfigFile("test");
        assertNotNull(MockUtils.replayBody(configFile));

        // merge case
        configFile.setNeedMerge(true);
        Mockito.when(ReplayMatcher.match(any(), any())).thenReturn(configFile);
        assertNull(MockUtils.replayBody(configFile));
    }

    @Test
    void checkResponseMocker() {
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

        // test exceed size limit
        dynamicClass.getTargetResponse().setBody(null);
        dynamicClass.getTargetResponse().setAttribute(ArexConstants.EXCEED_MAX_SIZE_FLAG, true);
        assertFalse(MockUtils.checkResponseMocker(dynamicClass));
    }

    @Test
    void createMocker() {
        configBuilder.build();
        ArexMocker actualResult = MockUtils.createMessageProducer("message-subject");
        assertEquals(MockCategoryType.MESSAGE_PRODUCER, actualResult.getCategoryType());

        actualResult = MockUtils.createMessageConsumer("message-subject");
        assertEquals(MockCategoryType.MESSAGE_CONSUMER, actualResult.getCategoryType());

        actualResult = MockUtils.createConfigFile("config-key");
        assertEquals(MockCategoryType.CONFIG_FILE, actualResult.getCategoryType());

        actualResult = MockUtils.createHttpClient("/api/test");
        assertEquals(MockCategoryType.HTTP_CLIENT, actualResult.getCategoryType());

        actualResult = MockUtils.createDynamicClass("test", "test");
        assertEquals(MockCategoryType.DYNAMIC_CLASS, actualResult.getCategoryType());

        actualResult = MockUtils.createDatabase("query");
        assertEquals(MockCategoryType.DATABASE, actualResult.getCategoryType());

        actualResult = MockUtils.createRedis("get");
        assertEquals(MockCategoryType.REDIS, actualResult.getCategoryType());

        actualResult = MockUtils.createServlet("/api/test");
        assertEquals(MockCategoryType.SERVLET, actualResult.getCategoryType());

        actualResult = MockUtils.createDubboConsumer("query");
        assertEquals(MockCategoryType.DUBBO_CONSUMER, actualResult.getCategoryType());

        actualResult = MockUtils.createDubboProvider("query");
        assertEquals(MockCategoryType.DUBBO_PROVIDER, actualResult.getCategoryType());

        actualResult = MockUtils.createDubboStreamProvider("query");
        assertEquals(MockCategoryType.DUBBO_STREAM_PROVIDER, actualResult.getCategoryType());

        actualResult = MockUtils.createNettyProvider("query");
        assertEquals(MockCategoryType.NETTY_PROVIDER, actualResult.getCategoryType());
    }
}
