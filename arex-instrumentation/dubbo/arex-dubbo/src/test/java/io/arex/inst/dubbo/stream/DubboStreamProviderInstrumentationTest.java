package io.arex.inst.dubbo.stream;

import io.arex.inst.dubbo.DubboProviderExtractor;
import io.arex.inst.runtime.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DubboStreamProviderInstrumentationTest {
    static DubboStreamProviderInstrumentation target;

    @BeforeAll
    static void setUp() {
        target = new DubboStreamProviderInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
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
    void startOnEnter() {
        try (MockedStatic<DubboProviderExtractor> extractor = Mockito.mockStatic(DubboProviderExtractor.class)) {
            DubboStreamProviderInstrumentation.StartAdvice.onEnter(null, null);
            extractor.verify(() -> DubboProviderExtractor.onServiceEnter(any(), any()));
        }
    }

    @Test
    void sendMessageOnExit() {
        try (MockedConstruction<DubboStreamProviderExtractor> mocked = Mockito.mockConstruction(DubboStreamProviderExtractor.class, (mock, context) -> {
        })) {
            DubboStreamProviderInstrumentation.SendMessageAdvice.onExit(null, null, null, null, null, null);
            DubboStreamProviderExtractor extractor = mocked.constructed().get(0);
            verify(extractor).record(any(), any(), any(), any(), any());
        }
    }

    @Test
    void onMessageOnEnter() {
        try (MockedConstruction<DubboStreamProviderExtractor> mocked = Mockito.mockConstruction(DubboStreamProviderExtractor.class, (mock, context) -> {
        })) {
            DubboStreamProviderInstrumentation.OnMessageAdvice.onEnter(null, null, null, null, null);
            DubboStreamProviderExtractor extractor = mocked.constructed().get(0);
            verify(extractor).replay(any(), any(), any(), any());
            verify(extractor).saveRequest(any());
        }
    }

    @Test
    void closeOnEnter() {
        try (MockedConstruction<DubboStreamProviderExtractor> mocked = Mockito.mockConstruction(DubboStreamProviderExtractor.class, (mock, context) -> {
        })) {
            DubboStreamProviderInstrumentation.CloseAdvice.onEnter(null, null, null, null, null, null);
            DubboStreamProviderExtractor extractor = mocked.constructed().get(0);
            verify(extractor).complete(any(), any(), any(), any(), any());
        }
    }
}