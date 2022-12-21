package io.arex.inst.httpclient.apache.async;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalHttpAsyncClientInstrumentationTest {
    static InternalHttpAsyncClientInstrumentation target;

    @BeforeAll
    static void setUp() {
        target = new InternalHttpAsyncClientInstrumentation();
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
        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class);
            MockedStatic<RepeatedCollectManager> repeatedCollectManager = mockStatic(RepeatedCollectManager.class);
            MockedStatic<FutureCallbackWrapper> futureCallbackWrapper = mockStatic(FutureCallbackWrapper.class)) {
            contextManager.when(ContextManager::needRecordOrReplay).thenReturn(false);
            boolean actResult = InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onEnter(null, null, null);
            Assertions.assertFalse(actResult);

            repeatedCollectManager.when(RepeatedCollectManager::validate).thenReturn(true);
            contextManager.when(ContextManager::needRecordOrReplay).thenReturn(true);
            contextManager.when(ContextManager::needReplay).thenReturn(true);

            FutureCallbackWrapper wrapper = Mockito.mock(FutureCallbackWrapper.class);
            Mockito.when(FutureCallbackWrapper.get(any(), any())).thenReturn(wrapper);
            Mockito.when(wrapper.replay()).thenReturn(MockResult.success("mock"));

            actResult = InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onEnter(null, null, null);
            Assertions.assertTrue(actResult);
        }
    }

    @Test
    void onExit() {
        FutureCallbackWrapper<?> callbackWrapper = new FutureCallbackWrapper<>(null, null);
        MockResult mockResult = MockResult.success("mock");
        assertDoesNotThrow(() -> {
            InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onExit(callbackWrapper, null, mockResult);
        });
    }
}