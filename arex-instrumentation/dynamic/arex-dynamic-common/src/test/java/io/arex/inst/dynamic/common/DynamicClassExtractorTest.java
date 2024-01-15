package io.arex.inst.dynamic.common;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.agent.thirdparty.util.time.DateFormatUtils;
import io.arex.inst.common.util.FluxUtil;
import io.arex.inst.dynamic.common.listener.MonoConsumer;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.arex.inst.runtime.util.sizeof.AgentSizeOf;
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
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    static AgentSizeOf agentSizeOf;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(Serializer.class);
        ConfigBuilder.create("test").enableDebug(true).build();
        agentSizeOf = Mockito.mock(AgentSizeOf.class);
        Mockito.mockStatic(AgentSizeOf.class);
        Mockito.when(AgentSizeOf.newInstance()).thenReturn(agentSizeOf);
    }

    @AfterAll
    static void tearDown() {
        agentSizeOf = null;
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
            Method testWithArexMock = getMethod(result);
            DynamicClassExtractor extractor = new DynamicClassExtractor(testWithArexMock, args);
            extractor.recordResponse(result);

            assertTrue(predicate.test(result));
        }
    }

    static Stream<Arguments> recordCase() {
        ArexContext context = Mockito.mock(ArexContext.class);
        Mockito.when(ContextManager.currentContext()).thenReturn(context);
        Runnable signatureContains = () -> {
            Set<Integer> methodSignatureHashList = new HashSet<>();
            methodSignatureHashList.add(StringUtil.encodeAndHash(
                    "io.arex.inst.dynamic.common.DynamicClassExtractorTest_testWithArexMock_mock Serializer.serialize_has_result_java.lang.String"
            ));
            Mockito.when(context.getMethodSignatureHashList()).thenReturn(methodSignatureHashList);
            try {
                Mockito.when(Serializer.serializeWithException(any(), anyString())).thenReturn("mock Serializer.serialize");
            } catch (Throwable ignored) {
            }
        };

        Runnable resultIsNull = () -> {
            Mockito.when(context.getMethodSignatureHashList()).thenReturn(new HashSet<>());
        };

        Predicate<Object> isNull = Objects::isNull;
        Predicate<Object> nonNull = Objects::nonNull;
        return Stream.of(
                arguments(signatureContains, new Object[]{"mock"}, "mock1", nonNull),
                arguments(resultIsNull, new Object[]{"mock"}, null, isNull),
                arguments(resultIsNull, new Object[]{"mock"}, Collections.singletonList("mock"), nonNull),
                arguments(resultIsNull, new Object[]{"mock"}, Collections.singletonMap("key", "val"), nonNull),
                arguments(resultIsNull, new Object[]{"mock"}, new int[1001], nonNull),
                arguments(resultIsNull, null, null, isNull),
                arguments(resultIsNull, null, Mono.just("mono test"), nonNull),
                arguments(resultIsNull, null, Futures.immediateFuture("mock-future"), nonNull),
                arguments(resultIsNull, null, Flux.just("mock-exception"), nonNull)
        );
    }

    private Method getMethod(Object result) {
        try {

            if (result instanceof Mono) {
                return Mono.class.getDeclaredMethod("just", Object.class);
            } else if (result instanceof Flux) {
                return Flux.class.getDeclaredMethod("just", Object.class);

            } else {
                return DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock", String.class);
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Test
    void resetMonoResponse() {
        try {
            Method testWithArexMock = DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock",
                String.class);
            final Object[] args = {"errorSerialize"};
            final DynamicClassExtractor extractor = new DynamicClassExtractor(testWithArexMock, args);
            final Predicate<Object> nonNull = Objects::nonNull;

            // exception
            Mono<?> result = monoExceptionTest();
            MonoConsumer monoConsumer = new MonoConsumer(extractor);
            result = monoConsumer.accept(result);
            result.subscribe();
            assertTrue(nonNull.test(result));

            // normal
            result = monoTest();
            result = monoConsumer.accept(result);
            result.subscribe();
            assertTrue(nonNull.test(result));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("replayCase")
    void replay(Runnable mocker, Object[] args, Predicate<MockResult> predicate) throws Throwable {
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
            mockService.when(() -> MockUtils.replayMocker(any(), any())).thenReturn(arexMocker2);

            Mockito.when(Serializer.serializeWithException(any(), anyString())).thenReturn("mock Serializer.serialize");
            Mockito.when(Serializer.serializeWithException(anyString(), anyString())).thenReturn("");
            Mockito.when(Serializer.deserialize(anyString(), anyString(), anyString())).thenReturn("mock result");
            Method testWithArexMock = DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock", String.class);

            DynamicClassExtractor extractor = new DynamicClassExtractor(testWithArexMock, args, "#val", null);
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
            arguments(needReplay, null, predicate2),
            arguments(needReplay, new Object[]{"mock"}, predicate2)
        );
    }

    @Test
    void testSetFutureResponse() throws NoSuchMethodException {
        Set<Integer> methodSignatureHashList = new HashSet<>();
        methodSignatureHashList.add(StringUtil.encodeAndHash(
            "io.arex.inst.dynamic.common.DynamicClassExtractorTest_testReturnListenableFuture_mock_has_result_java.lang.String"
        ));
        ArexContext context = Mockito.mock(ArexContext.class);
        Mockito.when(ContextManager.currentContext()).thenReturn(context);
        Mockito.when(context.getMethodSignatureHashList()).thenReturn(methodSignatureHashList);

        Method testReturnListenableFuture = DynamicClassExtractorTest.class.getDeclaredMethod("testReturnListenableFuture", String.class, Throwable.class);
        DynamicClassExtractor extractor = new DynamicClassExtractor(testReturnListenableFuture, new Object[]{"mock", null}, "#val", null);

        // result instanceOf CompletableFuture
        CompletableFuture<String> completableFuture = CompletableFuture.completedFuture("CompletableFuture-result");
        extractor.setFutureResponse(completableFuture);
        assertDoesNotThrow(() -> extractor.getSerializedResult());

        // result instanceOf ListenableFuture
        ListenableFuture<String> resultFuture = Futures.immediateFuture("result");
        extractor.setFutureResponse(resultFuture);
        assertDoesNotThrow(() -> extractor.getSerializedResult());
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

        //mono value
        Method testReturnMono = DynamicClassExtractorTest.class.getDeclaredMethod("testReturnMono", String.class,
            Throwable.class);
        DynamicClassExtractor monoTestExtractor = new DynamicClassExtractor(testReturnMono, new Object[]{"mock"},
            "#val", null);
        Object monoTestExtractorActualResult = monoTestExtractor.restoreResponse("test-value");
        assertEquals("test-value", ((Mono<?>) monoTestExtractorActualResult).block());

        monoTestExtractorActualResult = monoTestExtractor.restoreResponse(new RuntimeException("test-exception"));
        Object monoTestFinalActualResult = monoTestExtractorActualResult;
        assertThrows(RuntimeException.class, () -> ((Mono<?>) monoTestFinalActualResult).block());

        // flux value
        Method testReturnFlux = DynamicClassExtractorTest.class.getDeclaredMethod("testReturnFlux", String.class,
            Throwable.class);
        DynamicClassExtractor fluxTestExtractor = new DynamicClassExtractor(testReturnFlux, new Object[]{"mock"},
            "#val", null);
        List<FluxUtil.FluxElementResult> list = new ArrayList<>();
        FluxUtil.FluxResult fluxResult = new FluxUtil.FluxResult(null, list);
        Object fluxNormalTest = fluxTestExtractor.restoreResponse(fluxResult);
        assertNull(((Flux<?>) fluxNormalTest).blockFirst());

        Object fluxExceptionTest = fluxTestExtractor.restoreResponse(new RuntimeException());
        assertThrows(RuntimeException.class,()-> ((Flux<?>) fluxExceptionTest).blockFirst());
    }

    @Test
    void testBuildResultClazz() throws NoSuchMethodException {
        Method testWithArexMock = DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock", String.class);
        DynamicClassExtractor extractor = new DynamicClassExtractor(testWithArexMock, new Object[]{"mock"}, "#val", LocalDateTime.class);

        // result clazz is emtpy
        String actualResult = extractor.buildResultClazz("");
        assertEquals("", actualResult);

        // resultClazz contains -
        actualResult = extractor.buildResultClazz("Java.util.List-java.lang.String");
        assertEquals("Java.util.List-java.lang.String", actualResult);

        // @ArexMock actualType not null
        actualResult = extractor.buildResultClazz("Java.util.List");
        assertEquals("Java.util.List-java.time.LocalDateTime", actualResult);

        ConfigBuilder.create("mock-service").enableDebug(true).build();
        extractor = new DynamicClassExtractor(testWithArexMock, new Object[]{"mock"}, "#val", null);
        // DynamicEntityMap is empty
        actualResult = extractor.buildResultClazz("Java.util.List");
        assertEquals("Java.util.List", actualResult);

        // DynamicEntityMap is not empty, actualType is empty
        List<DynamicClassEntity> list = new ArrayList<>();
        list.add(new DynamicClassEntity("io.arex.inst.dynamic.common.DynamicClassExtractorTest", "testWithArexMock", "mock", ""));
        ConfigBuilder.create("mock-service").enableDebug(true).dynamicClassList(list).build();
        actualResult = extractor.buildResultClazz("Java.util.List");
        assertEquals("Java.util.List", actualResult);

        // actualType is not empty
        list.clear();
        list.add(new DynamicClassEntity("io.arex.inst.dynamic.common.DynamicClassExtractorTest", "testWithArexMock", "mock", "T:java.lang.String"));
        ConfigBuilder.create("mock-service").enableDebug(true).dynamicClassList(list).build();
        actualResult = extractor.buildResultClazz("Java.util.List");
        assertEquals("Java.util.List-java.lang.String", actualResult);
    }

    @Test
    void testBuildMethodKey() throws Throwable {
        Method testWithArexMock = DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock", String.class);
        DynamicClassExtractor extractor = new DynamicClassExtractor(testWithArexMock, null, "#val", String.class);

        // args is empty
        String actualResult = extractor.buildMethodKey(testWithArexMock, new Object[0]);
        assertNull(actualResult);

        // getDynamicClassSignatureMap is empty
        ConfigBuilder.create("mock-service").enableDebug(true).build();
        Mockito.when(Serializer.serializeWithException(any(), anyString())).thenReturn("mock Serializer.serialize");
        actualResult = extractor.buildMethodKey(testWithArexMock, new Object[]{"mock"});
        assertEquals("mock Serializer.serialize", actualResult);

        // getDynamicClassSignatureMap is not empty, additionalSignature is empty
        List<DynamicClassEntity> list = new ArrayList<>();
        list.add(new DynamicClassEntity("io.arex.inst.dynamic.common.DynamicClassExtractorTest", "testWithArexMock", "mock", ""));
        ConfigBuilder.create("mock-service").enableDebug(true).dynamicClassList(list).build();
        actualResult = extractor.buildMethodKey(testWithArexMock, new Object[]{"mock"});
        assertEquals("mock Serializer.serialize", actualResult);

        // additionalSignature is not empty
        list.clear();
        list.add(new DynamicClassEntity("io.arex.inst.dynamic.common.DynamicClassExtractorTest", "testWithArexMock", "", "$1"));
        ConfigBuilder.create("mock-service").enableDebug(true).dynamicClassList(list).build();
        actualResult = extractor.buildMethodKey(testWithArexMock, new Object[]{"mock-method-key"});
        assertEquals("mock-method-key", actualResult);

        // additionalSignature is not empty
        extractor = new DynamicClassExtractor(testWithArexMock, new Object[]{"mock"}, "#val", String.class);
        list.clear();
        list.add(new DynamicClassEntity("io.arex.inst.dynamic.common.DynamicClassExtractorTest", "testWithArexMock", "mock", "$1"));
        ConfigBuilder.create("mock-service").enableDebug(true).dynamicClassList(list).build();
        actualResult = extractor.buildMethodKey(testWithArexMock, new Object[]{"mock-method-key"});
        assertEquals("mock-method-key", actualResult);

        // express is null
        Method testWithArexMockList = DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock", List.class);
        extractor = new DynamicClassExtractor(testWithArexMockList, new Object[]{new ArrayList<>()}, null, String.class);
        list.clear();
        list.add(new DynamicClassEntity("io.arex.inst.dynamic.common.DynamicClassExtractorTest", "testWithArexMock", "mock", "$1.get(0)"));
        ConfigBuilder.create("mock-service").enableDebug(true).dynamicClassList(list).build();
        actualResult = extractor.buildMethodKey(testWithArexMockList, new Object[]{new ArrayList<>()});
        assertNull(actualResult);
    }

    public String testWithArexMock(String val) {
        return val + "testWithArexMock";
    }

    public String testWithArexMock(List list) {
       return "testWithArexMock";
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

    @Test
    void invalidOperation() throws Throwable {
        Method testWithArexMock = DynamicClassExtractorTest.class.getDeclaredMethod("testWithArexMock", String.class);
        final Object[] args = {"errorSerialize"};
        ConfigBuilder.create("invalid-operation").enableDebug(true).build();
        Mockito.when(Serializer.serializeWithException(any(), anyString())).thenThrow(new RuntimeException("errorSerialize"));
        DynamicClassExtractor extractor = new DynamicClassExtractor(testWithArexMock, args);
        extractor.recordResponse("errorSerialize");
        // invalid operation return empty
        DynamicClassExtractor extractor2 = new DynamicClassExtractor(testWithArexMock, args);
        final Field methodKey = DynamicClassExtractor.class.getDeclaredField("methodKey");
        methodKey.setAccessible(true);
        assertNull(methodKey.get(extractor2));

        assertNull(extractor.getSerializedResult());
        // invalid operation replay return ignore
        final MockResult replay = extractor.replay();
        assertEquals(MockResult.IGNORE_MOCK_RESULT, replay);
    }

    @Test
    void emptyMethodKeyAndExceedSize() throws NoSuchMethodException {
        Method testEmptyArgs = DynamicClassExtractorTest.class.getDeclaredMethod("invalidOperation");
        DynamicClassExtractor extractor = new DynamicClassExtractor(testEmptyArgs, new Object[0]);
        assertDoesNotThrow(() -> extractor.recordResponse(new int[1001]));
    }

    @Test
    void normalizeArgsTest() throws Exception {
        Method testEmptyArgs = DynamicClassExtractorTest.class.getDeclaredMethod("normalizeArgsTest");
        DynamicClassExtractor extractor = new DynamicClassExtractor(testEmptyArgs, new Object[0]);
        Method normalizeArgsMethod = DynamicClassExtractor.class.getDeclaredMethod("normalizeArgs", Object[].class);
        normalizeArgsMethod.setAccessible(true);

        String zeroSecond = "00.000";

        // null
        Object[] args = new Object[]{null};
        Object[] normalizedArgs = (Object[]) normalizeArgsMethod.invoke(extractor, new Object[]{args});
        assertNull(normalizedArgs[0]);

        // String
        args = new Object[]{"test"};
        normalizedArgs = (Object[]) normalizeArgsMethod.invoke(extractor, new Object[]{args});
        assertEquals("test", normalizedArgs[0]);

        // LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.now();
        normalizedArgs = (Object[]) normalizeArgsMethod.invoke(extractor, new Object[]{new Object[]{localDateTime}});
        String text = DateFormatUtils.format(localDateTime, "yyyy-MM-dd HH:mm:ss.SSS");
        assertEquals(text.substring(0, text.length() - zeroSecond.length()) + zeroSecond, normalizedArgs[0]);
        System.out.println("localDateTime: " + normalizedArgs[0]);

        // LocalTime
        LocalTime localTime = LocalTime.now();
        normalizedArgs = (Object[]) normalizeArgsMethod.invoke(extractor, new Object[]{new Object[]{localTime}});
        text = DateFormatUtils.format(localTime, "HH:mm:ss.SSS");
        assertEquals(text.substring(0, text.length() - zeroSecond.length()) + zeroSecond, normalizedArgs[0]);
        System.out.println("localTime: " + normalizedArgs[0]);

        // Calendar
        String zeroSecondWithZone = "00.000+08:00";
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-01:00"));
        normalizedArgs = (Object[]) normalizeArgsMethod.invoke(extractor, new Object[]{new Object[]{calendar}});
        text = DateFormatUtils.format(calendar, "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", calendar.getTimeZone());
        assertEquals(text.substring(0, text.length() - zeroSecondWithZone.length()) + "00.000-01:00", normalizedArgs[0]);
        System.out.println("calendar: " + normalizedArgs[0]);

        // Date
        Date date = new Date();
        normalizedArgs = (Object[]) normalizeArgsMethod.invoke(extractor, new Object[]{new Object[]{date}});
        text = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss.SSS");
        assertEquals(text.substring(0, text.length() - zeroSecond.length()) + zeroSecond, normalizedArgs[0]);
        System.out.println("date: " + normalizedArgs[0]);

        // joda LocalDateTime
        org.joda.time.LocalDateTime jodaLocalDateTime = org.joda.time.LocalDateTime.now();
        String originalTimeString = jodaLocalDateTime.toString("yyyy-MM-dd HH:mm:ss.SSS");
        args = new Object[]{jodaLocalDateTime};
        normalizedArgs = (Object[]) normalizeArgsMethod.invoke(extractor, new Object[]{args});
        assertEquals(originalTimeString.substring(0, originalTimeString.length() - zeroSecond.length()) + zeroSecond, normalizedArgs[0]);
        System.out.println("jodaLocalDateTime: " + normalizedArgs[0]);

        // joda LocalTime
        org.joda.time.LocalTime jodaLocalTime = org.joda.time.LocalTime.now();
        originalTimeString = jodaLocalTime.toString("HH:mm:ss.SSS");
        args = new Object[]{jodaLocalTime};
        normalizedArgs = (Object[]) normalizeArgsMethod.invoke(extractor, new Object[]{args});
        assertEquals(originalTimeString.substring(0, originalTimeString.length() - zeroSecond.length()) + zeroSecond, normalizedArgs[0]);
        System.out.println("jodaLocalTime: " + normalizedArgs[0]);
    }

    public Mono<String> testReturnMono(String val, Throwable t) {
        if (t != null) {
            return Mono.error(t);
        }
        return Mono.justOrEmpty(val + "testReturnMono");
    }

    public Flux<String> testReturnFlux(String val,Throwable t){
        if (t != null) {
            return Flux.error(t);
        }
        return val == null ? Flux.empty() : Flux.just(val + "testReturnFlux");
    }

    public static Mono<String> monoTest() {
        return Mono.justOrEmpty("Mono test")
            .doOnNext(value -> System.out.println("Mono context:" + value))
            .onErrorResume(t -> Mono.empty());
    }

    public static Mono<Object> monoExceptionTest() {
        return Mono.error(new RuntimeException("e"))
            .doOnError(throwable -> System.out.println("Mono error:" + throwable))
            .doOnSuccess(object -> System.out.println("Mono success:" + object.getClass()));
    }

    public static Flux<String> fluxTest() {
        return Flux.just("flux","test")
            .doOnNext(value -> System.out.println("Flux context:" + value))
            .onErrorResume(t -> Mono.empty());
    }

    public static Flux<String> fluxOnErrorTest() {
        return Flux.just("flux", "test")
            .doOnNext(value -> {
                throw new RuntimeException("error");
            });
    }

    public static Flux<Object> fluxExceptionTest() {
        return Flux.error(new RuntimeException("e"))
            .doOnError(throwable -> System.out.println("Flux error:" + throwable))
            .doOnNext(object -> System.out.println("Flux success:" + object.getClass()));
    }
}
