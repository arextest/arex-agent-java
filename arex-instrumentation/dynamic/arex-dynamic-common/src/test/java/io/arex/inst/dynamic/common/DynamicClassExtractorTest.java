package io.arex.inst.dynamic.common;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class DynamicClassExtractorTest {
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(Serializer.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("recordCase")
    void record(Runnable mocker, Object[] args, Object result, Predicate<Object> predicate)
        throws NoSuchMethodException {
        mocker.run();

        try (MockedStatic<MockUtils> mockService = mockStatic(MockUtils.class)) {
            ArexMocker arexMocker = new ArexMocker();
            arexMocker.setTargetRequest(new Target());
            arexMocker.setTargetResponse(new Target());
            mockService.when(() -> MockUtils.createDynamicClass(any(), any())).thenReturn(arexMocker);
            mockService.when(() -> MockUtils.recordMocker(any())).then((Answer<Void>) invocationOnMock -> {
                System.out.println("mock MockService.recordMocker");
                return null;
            });

            Method testWithArexMock = DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock", String.class);
            DynamicClassExtractor extractor = new DynamicClassExtractor(testWithArexMock, args);

            extractor.recordResponse(result);
            assertTrue(predicate.test(result));
        }
    }

    static Stream<Arguments> recordCase() {
        ArexContext context = Mockito.mock(ArexContext.class);
        Mockito.when(ContextManager.currentContext()).thenReturn(context);
        Runnable signatureContains = () -> {
            List<Integer> methodSignatureHashList = new ArrayList<>();
            methodSignatureHashList.add(StringUtil.encodeAndHash(
                "io.arex.inst.dynamic.common.DynamicClassExtractorTest_testWithArexMock_mock Serializer.serialize_no_result"
            ));
            Mockito.when(context.getMethodSignatureHashList()).thenReturn(methodSignatureHashList);
            Mockito.when(Serializer.serialize(any(), anyString())).thenReturn("mock Serializer.serialize");
        };

        Runnable resultIsNull = () -> {
            Mockito.when(context.getMethodSignatureHashList()).thenReturn(new ArrayList<>());
        };

        Predicate<Object> isNull = Objects::isNull;
        Predicate<Object> nonNull = Objects::nonNull;
        return Stream.of(
            arguments(signatureContains, new Object[]{"mock"}, "mock1", nonNull),
            arguments(resultIsNull, new Object[]{"mock"}, null, isNull),
            arguments(resultIsNull, null, Collections.singletonList("mock"), nonNull),
            arguments(resultIsNull, null, Collections.singletonMap("key", "val"), nonNull),
            arguments(resultIsNull, null, new int[1001], nonNull),
            arguments(resultIsNull, null, null, isNull),
            arguments(resultIsNull, null, Futures.immediateFuture("mock-future"), nonNull)
        );
    }

    @ParameterizedTest
    @MethodSource("replayCase")
    void replay(Runnable mocker, Object[] args, Predicate<MockResult> predicate) throws NoSuchMethodException {
        mocker.run();

        try (MockedStatic<MockUtils> mockService = mockStatic(MockUtils.class);
            MockedStatic<IgnoreUtils> ignoreService = mockStatic(IgnoreUtils.class)) {
            ignoreService.when(() -> IgnoreUtils.ignoreMockResult(any(), any())).thenReturn(true);

            ArexMocker arexMocker = new ArexMocker();
            arexMocker.setTargetRequest(new Target());
            arexMocker.setTargetResponse(new Target());
            mockService.when(() -> MockUtils.createDynamicClass(any(), any())).thenReturn(arexMocker);
            mockService.when(() -> MockUtils.checkResponseMocker(any())).thenReturn(true);

            ArexMocker arexMocker2 = new ArexMocker();
            arexMocker2.setTargetRequest(new Target());
            arexMocker2.setTargetResponse(new Target());
            arexMocker2.getTargetResponse().setBody("mock Body");
            arexMocker2.getTargetResponse().setType("mock Type");
            mockService.when(() -> MockUtils.replayMocker(any())).thenReturn(arexMocker2);

            Mockito.when(Serializer.serialize(any(), anyString())).thenReturn("mock Serializer.serialize");
            Mockito.when(Serializer.serialize(anyString(), anyString())).thenReturn("");
            Method testWithArexMock = DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock", String.class);

            DynamicClassExtractor extractor = new DynamicClassExtractor(testWithArexMock, new Object[]{"mock"}, "#val", null);
            MockResult mockResult = extractor.replay();
            assertTrue(predicate.test(mockResult));
        }
    }

    static Stream<Arguments> replayCase() {
        Runnable needReplay = () -> {
            Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of(""));
        };
        Predicate<MockResult> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(needReplay, new Object[]{"mock"}, predicate2)
        );
    }

    @Test
    void testSetFutureResponse() throws NoSuchMethodException {
        List<Integer> methodSignatureHashList = new ArrayList<>();
        methodSignatureHashList.add(StringUtil.encodeAndHash(
            "io.arex.inst.dynamic.common.DynamicClassExtractorTest_testReturnListenableFuture_mock_no_result"
        ));
        ArexContext context = Mockito.mock(ArexContext.class);
        Mockito.when(ContextManager.currentContext()).thenReturn(context);
        Mockito.when(context.getMethodSignatureHashList()).thenReturn(methodSignatureHashList);

        Method testReturnListenableFuture = DynamicClassExtractorTest.class.getDeclaredMethod("testReturnListenableFuture", String.class, Throwable.class);
        DynamicClassExtractor extractor = new DynamicClassExtractor(testReturnListenableFuture, new Object[]{"mock", null}, "#val", null);

        // result instanceOf CompletableFuture
        CompletableFuture<String> completableFuture = CompletableFuture.completedFuture("CompletableFuture-result");
        extractor.setFutureResponse(completableFuture);
        assertNull(extractor.getSerializedResult());

        // result instanceOf ListenableFuture
        ListenableFuture<String> resultFuture = Futures.immediateFuture("result");
        extractor.setFutureResponse(resultFuture);
        assertNull(extractor.getSerializedResult());
    }

    @Test
    void restoreResponseTest() throws NoSuchMethodException, ExecutionException, InterruptedException {
        // ListenableFuture
        Method testReturnListenableFuture = DynamicClassExtractorTest.class.getDeclaredMethod("testReturnListenableFuture", String.class, Throwable.class);
        DynamicClassExtractor extractor = new DynamicClassExtractor(testReturnListenableFuture, new Object[]{"mock"}, "#val", null);

        Object actualResult = extractor.restoreResponse("test-value");
        assertEquals("test-value", ((ListenableFuture<?>)actualResult).get());

        actualResult = extractor.restoreResponse(new RuntimeException("test-exception"));
        Object finalActualResult = actualResult;
        assertThrows(ExecutionException.class, () -> ((ListenableFuture<?>) finalActualResult).get());

        // CompletableFuture
        Method testReturnCompletableFuture = DynamicClassExtractorTest.class.getDeclaredMethod("testReturnCompletableFuture", String.class, Throwable.class);
        extractor = new DynamicClassExtractor(testReturnCompletableFuture, new Object[]{"mock"}, "#val", null);

        actualResult = extractor.restoreResponse("test-value");
        assertEquals("test-value", ((CompletableFuture<?>)actualResult).get());

        actualResult = extractor.restoreResponse(new RuntimeException("test-exception"));
        Object finalActualResult1 = actualResult;
        assertThrows(ExecutionException.class, () -> ((CompletableFuture<?>) finalActualResult1).get());

        // normal value
        Method testWithArexMock = DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock", String.class);
        extractor = new DynamicClassExtractor(testWithArexMock, new Object[]{"mock"}, "#val", null);
        actualResult = extractor.restoreResponse("test-value");
        assertEquals("test-value", actualResult);
    }

    @Test
    void testBuildResultClazz() throws NoSuchMethodException {
        Method testWithArexMock = DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock", String.class);
        DynamicClassExtractor extractor = new DynamicClassExtractor(testWithArexMock, new Object[]{"mock"}, "#val", String.class);

        // result clazz is emtpy
        String actualResult = extractor.buildResultClazz("");
        assertEquals("", actualResult);

        // resultClazz contains -
        actualResult = extractor.buildResultClazz("Java.util.List-java.lang.String");
        assertEquals("Java.util.List-java.lang.String", actualResult);

        // @ArexMock actualType not null
        actualResult = extractor.buildResultClazz("Java.util.List");
        assertEquals("Java.util.List-java.lang.String", actualResult);

        ConfigBuilder.create("mock-service").build();
        extractor = new DynamicClassExtractor(testWithArexMock, new Object[]{"mock"}, "#val", null);
        // DynamicEntityMap is empty
        actualResult = extractor.buildResultClazz("Java.util.List");
        assertEquals("Java.util.List", actualResult);

        // DynamicEntityMap is not empty, actualType is empty
        List<DynamicClassEntity> list = new ArrayList<>();
        list.add(new DynamicClassEntity("io.arex.inst.dynamic.common.DynamicClassExtractorTest", "testWithArexMock", "mock", ""));
        ConfigBuilder.create("mock-service").dynamicClassList(list).build();
        actualResult = extractor.buildResultClazz("Java.util.List");
        assertEquals("Java.util.List", actualResult);

        // actualType is not empty
        list.clear();
        list.add(new DynamicClassEntity("io.arex.inst.dynamic.common.DynamicClassExtractorTest", "testWithArexMock", "mock", "T:java.lang.String"));
        ConfigBuilder.create("mock-service").dynamicClassList(list).build();
        actualResult = extractor.buildResultClazz("Java.util.List");
        assertEquals("Java.util.List-java.lang.String", actualResult);
    }

    @Test
    void testBuildMethodKey() throws NoSuchMethodException {
        Method testWithArexMock = DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock", String.class);
        DynamicClassExtractor extractor = new DynamicClassExtractor(testWithArexMock, new Object[]{"mock"}, "#val", String.class);

        // args is empty
        String actualResult = extractor.buildMethodKey(testWithArexMock, new Object[0]);
        assertNull(actualResult);

        // DynamicEntityMap is empty
        ConfigBuilder.create("mock-service").build();
        Mockito.when(Serializer.serialize(any(), anyString())).thenReturn("mock Serializer.serialize");
        actualResult = extractor.buildMethodKey(testWithArexMock, new Object[]{"mock"});
        assertEquals("mock Serializer.serialize", actualResult);

        // DynamicEntityMap is not empty, additionalSignature is empty
        List<DynamicClassEntity> list = new ArrayList<>();
        list.add(new DynamicClassEntity("io.arex.inst.dynamic.common.DynamicClassExtractorTest", "testWithArexMock", "mock", ""));
        ConfigBuilder.create("mock-service").dynamicClassList(list).build();
        actualResult = extractor.buildMethodKey(testWithArexMock, new Object[]{"mock"});
        assertEquals("mock Serializer.serialize", actualResult);

        // additionalSignature is not empty
        list.clear();
        list.add(new DynamicClassEntity("io.arex.inst.dynamic.common.DynamicClassExtractorTest", "testWithArexMock", "mock", "$1"));
        ConfigBuilder.create("mock-service").dynamicClassList(list).build();
        actualResult = extractor.buildMethodKey(testWithArexMock, new Object[]{"mock-method-key"});
        assertEquals("mock-method-key", actualResult);
    }

    public String testWithArexMock(String val) {
        return val + "testWithArexMock";
    }

    public ListenableFuture<String> testReturnListenableFuture(String val, Throwable t) {
        if (t != null) {
            return Futures.immediateFailedFuture(t);
        }
        return Futures.immediateFuture(val + "testReturnListenableFuture");
    }

    public CompletableFuture<String> testReturnCompletableFuture(String val, Throwable t) {
        if (t != null) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(t);
            return future;
        }
        return CompletableFuture.completedFuture(val + "testReturnCompletableFuture");
    }
}