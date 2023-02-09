package io.arex.inst.dubbo.stream;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import org.apache.dubbo.rpc.protocol.tri.RequestMetadata;
import org.junit.jupiter.api.AfterAll;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DubboStreamConsumerInstrumentationTest {
    static DubboStreamConsumerInstrumentation target;
    static DubboStreamConsumerExtractor extractor;

    @BeforeAll
    static void setUp() {
        target = new DubboStreamConsumerInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.mockStatic(RepeatedCollectManager.class);
        extractor = Mockito.mock(DubboStreamConsumerExtractor.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        extractor = null;
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
    void sendMessageOnEnter() {
        try (MockedConstruction<DubboStreamConsumerExtractor> mocked = Mockito.mockConstruction(DubboStreamConsumerExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay(any(), any())).thenReturn(Collections.singletonList(MockResult.success("mock")));
        })) {
            assertTrue(DubboStreamConsumerInstrumentation.SendMessageAdvice.onEnter(null, null, null, null, null));
        }
    }

    @ParameterizedTest
    @MethodSource("sendMessageOnExitCase")
    void sendMessageOnExit(Runnable mocker, List<MockResult> mockResults, Runnable asserts) {
        mocker.run();
        RequestMetadata requestMetadata = Mockito.mock(RequestMetadata.class);
        DubboStreamConsumerInstrumentation.SendMessageAdvice.onExit(null, null, requestMetadata, null, extractor, mockResults);
        asserts.run();
    }

    static Stream<Arguments> sendMessageOnExitCase() {
        Runnable emptyMocker = () -> {};
        Runnable exitAndValidate = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        };
        Runnable needRecord = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        Runnable asserts1 = () -> {
            verifyNoInteractions(extractor);
        };
        Runnable asserts2 = () -> {
            verify(extractor).doReplay(any(), any(), any());
        };
        Runnable asserts3 = () -> {
            verify(extractor).saveRequest(any(), any());
        };
        return Stream.of(
                arguments(emptyMocker, null, asserts1),
                arguments(exitAndValidate, Collections.singletonList(MockResult.success(null)), asserts2),
                arguments(needRecord, null, asserts3)
        );
    }

    @Test
    void onStartOnEnter() {
        try (MockedStatic<DubboStreamConsumerExtractor> consumerExtractor = Mockito.mockStatic(DubboStreamConsumerExtractor.class)) {
            DubboStreamConsumerInstrumentation.OnStartAdvice.onEnter(null);
            consumerExtractor.verify(() -> DubboStreamConsumerExtractor.init(any()));
        }
    }

    @Test
    void onMessageOnExit() {
        try (MockedConstruction<DubboStreamConsumerExtractor> mocked = Mockito.mockConstruction(DubboStreamConsumerExtractor.class, (mock, context) -> {
        })) {
            DubboStreamConsumerInstrumentation.OnMessageAdvice.onExit(null, null, null);
            DubboStreamConsumerExtractor consumerExtractor = mocked.constructed().get(0);
            verify(consumerExtractor).record(any(), any(), any());
        }
    }

    @Test
    void onCompleteOnEnter() {
        try (MockedConstruction<DubboStreamConsumerExtractor> mocked = Mockito.mockConstruction(DubboStreamConsumerExtractor.class, (mock, context) -> {
        })) {
            DubboStreamConsumerInstrumentation.OnCompleteAdvice.onEnter(null, null, null);
            DubboStreamConsumerExtractor consumerExtractor = mocked.constructed().get(0);
            verify(consumerExtractor).complete(any(), any());
        }
    }

    @Test
    void closeOnEnter() {
        try (MockedStatic<DubboStreamConsumerExtractor> consumerExtractor = Mockito.mockStatic(DubboStreamConsumerExtractor.class)) {
            DubboStreamConsumerInstrumentation.CloseAdvice.onEnter(null, null);
            consumerExtractor.verify(() -> DubboStreamConsumerExtractor.close(any(), any()));
        }
    }
}