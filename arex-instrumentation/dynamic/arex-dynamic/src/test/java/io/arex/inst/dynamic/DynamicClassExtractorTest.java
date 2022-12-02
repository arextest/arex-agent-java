package io.arex.inst.dynamic;

import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.model.DynamicClassMocker;
import io.arex.foundation.model.MockResult;
import io.arex.foundation.util.StringUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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
        try (MockedConstruction<DynamicClassMocker> mocked = Mockito.mockConstruction(DynamicClassMocker.class)) {
            mocker.run();
            DynamicClassExtractor extractor = new DynamicClassExtractor("clazzName", "operation", args, result);
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
        try (MockedConstruction<DynamicClassMocker> mocked = Mockito.mockConstruction(DynamicClassMocker.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn("mock");
        })) {
            mocker.run();
            DynamicClassExtractor extractor = new DynamicClassExtractor(null, null, args);
            MockResult mockResult = extractor.replay();
            assertTrue(predicate.test(mockResult));
        }
    }

    static Stream<Arguments> replayCase() {
        Runnable emptyMocker = () -> {};
        Runnable needReplay = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of(""));
        };
        Predicate<MockResult> predicate1 = Objects::isNull;
        Predicate<MockResult> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(emptyMocker, null, predicate1),
                arguments(needReplay, new Object[]{"mock"}, predicate2),
                arguments(emptyMocker, null, predicate2)
        );
    }
}