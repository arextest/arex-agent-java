package io.arex.inst.dynamic;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.inst.dynamic.listener.ResponseConsumer;
import io.arex.inst.dynamic.listener.ResponseFutureCallback;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class DynamicClassExtractorTest {
    static ArexContext context;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        context = Mockito.mock(ArexContext.class);
        Mockito.when(ContextManager.currentContext()).thenReturn(context);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("recordCase")
    void record(Runnable mocker, Object[] args, Object result, Predicate<Object> predicate) {
        mocker.run();

        try (MockedStatic<MockUtils> mockService = mockStatic(MockUtils.class);
            MockedStatic<Serializer> serializer = mockStatic(Serializer.class)) {

            ArexMocker arexMocker = new ArexMocker();
            arexMocker.setTargetRequest(new Target());
            arexMocker.setTargetResponse(new Target());
            mockService.when(() -> MockUtils.createDynamicClass(any(), any())).thenReturn(arexMocker);
            mockService.when(() -> MockUtils.recordMocker(any())).then((Answer<Void>) invocationOnMock -> {
                System.out.println("mock MockService.recordMocker");
                return null;
            });

            serializer.when(() -> Serializer.serialize(any(), anyString())).thenReturn("mock Serializer.serialize");

            DynamicClassExtractor extractor = new DynamicClassExtractor("clazzName", "operation", args, "returnType");
            extractor.setResponse(result);
            extractor.record();
            assertTrue(predicate.test(result));
        }
    }

    static Stream<Arguments> recordCase() {
        Runnable emptyMocker = () -> {};
        Runnable contextMocker1 = () -> {
            List<Integer> methodSignatureHashList = new ArrayList<>();
            methodSignatureHashList.add(StringUtil.encodeAndHash("clazzName_operation_[\"mock\"]_no_result"));
            Mockito.when(context.getMethodSignatureHashList()).thenReturn(methodSignatureHashList);
        };
        Predicate<Object> predicate1 = Objects::isNull;
        Predicate<Object> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(contextMocker1, new Object[]{"mock"}, null, predicate1),
                arguments(emptyMocker, new Object[]{"mock"}, "mock", predicate2),
                arguments(emptyMocker, null, null, predicate1),
                arguments(emptyMocker, null, Collections.singletonList("mock"), predicate2),
                arguments(emptyMocker, null, Collections.singletonMap("key", "val"), predicate2),
                arguments(emptyMocker, null, new int[1001], predicate2)
        );
    }

    @ParameterizedTest
    @MethodSource("replayCase")
    void replay(Runnable mocker, Object[] args, Predicate<MockResult> predicate) {
        mocker.run();


        try (MockedStatic<MockUtils> mockService = mockStatic(MockUtils.class);
            MockedStatic<IgnoreUtils> ignoreService = mockStatic(IgnoreUtils.class);
            MockedStatic<Serializer> serializer = mockStatic(Serializer.class)) {
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

            serializer.when(() -> Serializer.serialize(any(), anyString())).thenReturn("mock Serializer.serialize");
            serializer.when(() -> Serializer.deserialize(anyString(), anyString())).thenReturn(new Object());

            DynamicClassExtractor extractor = new DynamicClassExtractor(null, null, args, null);
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
    void testFuture() {
        AtomicReference<ResponseConsumer> atomicConsumer = new AtomicReference<>();
        AtomicReference<ResponseFutureCallback> atomicCallBack = new AtomicReference<>();
        try (MockedConstruction<ResponseConsumer> mocked = Mockito.mockConstruction(ResponseConsumer.class, (mock, context) -> {
            atomicConsumer.set(mock);
        })) {
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(
                    () -> System.out.println());
            new DynamicClassExtractor(null, null, null, null).setFutureResponse(completableFuture);
            Assertions.assertNotNull(atomicConsumer.get());
        }

        try (MockedConstruction<ResponseFutureCallback> mocked = Mockito.mockConstruction(ResponseFutureCallback.class, (mock, context) -> {
            atomicCallBack.set(mock);
        })) {
            ListenableFuture<?> listenableFuture = MoreExecutors.newDirectExecutorService()
                    .submit(() -> System.out.println());
            new DynamicClassExtractor(null, null, null, null).setFutureResponse(listenableFuture);
            Assertions.assertNotNull(atomicCallBack.get());
        }
    }

    @Test
    void futureCallBackTest() {
        AtomicReference<DynamicClassExtractor> extractorAtomicReference = new AtomicReference<>();
        try (MockedConstruction<DynamicClassExtractor> mocked = Mockito.mockConstruction(DynamicClassExtractor.class, (mock, context) -> {
            extractorAtomicReference.set(mock);
        })) {
            ResponseConsumer responseConsumer = new ResponseConsumer(
                    new DynamicClassExtractor(null, null, null, null));
            String result = "result";
            responseConsumer.accept(result, null);
            Assertions.assertNotNull(extractorAtomicReference.get());
            Mockito.verify(extractorAtomicReference.get(), Mockito.times(1)).setResponse(result);
            NullPointerException exception = new NullPointerException();
            responseConsumer.accept(null, exception);
            Mockito.verify(extractorAtomicReference.get(), Mockito.times(1)).setResponse(exception);

            ResponseFutureCallback responseFutureCallback = new ResponseFutureCallback(
                    new DynamicClassExtractor(null, null, null, null));
            responseFutureCallback.onSuccess(result);
            Mockito.verify(extractorAtomicReference.get(), Mockito.times(1)).setResponse(result);
            responseFutureCallback.onFailure(exception);
            Mockito.verify(extractorAtomicReference.get(), Mockito.times(1)).setResponse(exception);
        }
    }

    @Test
    void restoreResponseTest() {
        String listenableFuture = "com.google.common.util.concurrent.ListenableFuture";
        String completableFuture = "java.util.concurrent.CompletableFuture";
        String normalFuture = "java.util.concurrent.Future";
        List cntSuccess = new ArrayList<>();
        List cntFailure = new ArrayList<>();
        RuntimeException exception = new RuntimeException();
        DynamicClassExtractor dynamicClassExtractor = new DynamicClassExtractor(null, null, null,
                listenableFuture);
        Object result = dynamicClassExtractor.restoreResponse("result");
        Object exceptionResult = dynamicClassExtractor.restoreResponse(exception);
        Assertions.assertTrue(result instanceof ListenableFuture);
        Assertions.assertTrue(exceptionResult instanceof ListenableFuture);
        getResultFormListenFuture((ListenableFuture) result, cntSuccess, cntFailure);
        getResultFormListenFuture((ListenableFuture) exceptionResult, cntSuccess, cntFailure);

        DynamicClassExtractor dynamicClassExtractor2 = new DynamicClassExtractor(null, null, null,
                completableFuture);
        Object completableFutureResult = dynamicClassExtractor2.restoreResponse("result");
        Object completableFutureResultException = dynamicClassExtractor2.restoreResponse(exception);
        Assertions.assertTrue(completableFutureResult instanceof CompletableFuture);
        Assertions.assertTrue(completableFutureResultException instanceof CompletableFuture);
        getResultCompletableFuture((CompletableFuture) completableFutureResult, cntSuccess, cntFailure);
        getResultCompletableFuture((CompletableFuture) completableFutureResultException, cntSuccess, cntFailure);


        DynamicClassExtractor dynamicClassExtractor3 = new DynamicClassExtractor(null, null, null,
                normalFuture);
        Object futureResult = dynamicClassExtractor3.restoreResponse("result");
        Object futureResultException = dynamicClassExtractor.restoreResponse(exception);
        Assertions.assertTrue(futureResult instanceof ListenableFuture);
        Assertions.assertTrue(futureResultException instanceof ListenableFuture);


        getResultFormListenFuture((ListenableFuture) futureResult, cntSuccess, cntFailure);
        getResultFormListenFuture((ListenableFuture) futureResultException, cntSuccess, cntFailure);

        Assertions.assertEquals(3, cntSuccess.size());
        Assertions.assertEquals(3, cntFailure.size());

        DynamicClassExtractor dynamicClassExtractor4 = new DynamicClassExtractor(null, null, null,
                "java.lang.String");
        Object response = dynamicClassExtractor4.restoreResponse("result");
        Assertions.assertTrue(response instanceof String);
    }

    private void getResultCompletableFuture(CompletableFuture future2, List cntSuccess, List cntFailure) {
        future2.whenComplete((object, throwable) -> {
            if (throwable != null) {
                cntFailure.add("test");
            } else {
                cntSuccess.add("test");
            }
        });
    }

    private void getResultFormListenFuture(ListenableFuture future1, List cntSuccess, List cntFailure) {
        Futures.addCallback(future1, new FutureCallback() {
            @Override
            public void onSuccess(Object result) {
                cntSuccess.add("test");
            }

            @Override
            public void onFailure(Throwable t) {
                cntFailure.add("test");
            }
        }, MoreExecutors.directExecutor());
    }
}