package io.arex.inst.dubbo;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import org.apache.dubbo.rpc.Invocation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class DubboConsumerInstrumentationTest {
    static DubboConsumerInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new DubboConsumerInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.mockStatic(RepeatedCollectManager.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @Test
    void methodAdvices() {
        assertNotNull(target.methodAdvices());
    }

    @Test
    void adviceClassNames() {
        assertNotNull(target.adviceClassNames());
    }

    @Test
    void onEnter() {
        try (MockedConstruction<DubboConsumerExtractor> mocked = Mockito.mockConstruction(DubboConsumerExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success(false, null));
        })) {
            Invocation invocation = Mockito.mock(Invocation.class);
            Mockito.when(invocation.getProtocolServiceKey()).thenReturn(":tri");
            assertFalse(DubboConsumerInstrumentation.InvokeAdvice.onEnter(null, invocation, null, null));
            Mockito.when(invocation.getProtocolServiceKey()).thenReturn("mock");
            assertTrue(DubboConsumerInstrumentation.InvokeAdvice.onEnter(null, invocation, null, null));
        }
    }

    @ParameterizedTest
    @MethodSource("onExitCase")
    void onExit(Runnable mocker, MockResult mockResult, Predicate<MockResult> predicate) {
        mocker.run();
        DubboConsumerExtractor extractor = Mockito.mock(DubboConsumerExtractor.class);
        DubboConsumerInstrumentation.InvokeAdvice.onExit(null, extractor, mockResult);
        assertTrue(predicate.test(mockResult));
    }

    static Stream<Arguments> onExitCase() {
        Runnable emptyMocker = () -> {};
        Runnable exitAndValidate = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        };
        Runnable needRecord = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        Predicate<MockResult> predicate1 = Objects::isNull;
        Predicate<MockResult> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(emptyMocker, null, predicate1),
                arguments(exitAndValidate, MockResult.success(null), predicate2),
                arguments(needRecord, null, predicate1)
        );
    }
}