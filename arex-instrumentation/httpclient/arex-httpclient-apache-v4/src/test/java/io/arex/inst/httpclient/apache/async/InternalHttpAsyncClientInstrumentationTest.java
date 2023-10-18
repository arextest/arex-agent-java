package io.arex.inst.httpclient.apache.async;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.util.IgnoreUtils;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.jupiter.api.AfterAll;
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
    void onEnter() throws URISyntaxException {
        HttpUriRequest request1 = Mockito.mock(HttpUriRequest.class);
        Mockito.when(request1.getURI()).thenThrow(new RuntimeException("mock exception"));
        boolean actualResult = InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onEnter(request1, null, null);
        assertFalse(actualResult);

        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class);
            MockedStatic<RepeatedCollectManager> repeatedCollectManager = mockStatic(RepeatedCollectManager.class);
            MockedStatic<FutureCallbackWrapper> futureCallbackWrapper = mockStatic(FutureCallbackWrapper.class);
            MockedStatic<IgnoreUtils> ignoreUtils = mockStatic(IgnoreUtils.class)) {
            ignoreUtils.when(() -> IgnoreUtils.excludeOperation(any())).thenReturn(true);
            HttpUriRequest request2 = Mockito.mock(HttpUriRequest.class);
            Mockito.when(request2.getURI()).thenReturn(new URI("http://localhost"));
            actualResult = InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onEnter(request2, null, null);
            assertFalse(actualResult);

            ignoreUtils.when(() -> IgnoreUtils.excludeOperation(any())).thenReturn(false);
            contextManager.when(ContextManager::needRecordOrReplay).thenReturn(false);
            actualResult = InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onEnter(request2, null, null);
            assertFalse(actualResult);

            repeatedCollectManager.when(RepeatedCollectManager::validate).thenReturn(true);
            contextManager.when(ContextManager::needRecordOrReplay).thenReturn(true);
            contextManager.when(ContextManager::needReplay).thenReturn(true);

            FutureCallbackWrapper wrapper = Mockito.mock(FutureCallbackWrapper.class);
            Mockito.when(FutureCallbackWrapper.wrap(any(), any())).thenReturn(wrapper);
            Mockito.when(wrapper.replay()).thenReturn(MockResult.success("mock"));

            actualResult = InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onEnter(request2, null, null);
            assertTrue(actualResult);
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