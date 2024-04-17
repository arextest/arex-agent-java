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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import net.bytebuddy.description.type.TypeDescription;
import org.apache.http.HttpException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
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
    void typeMatcher() throws ClassNotFoundException {
        assertTrue(target.typeMatcher().matches(TypeDescription.ForLoadedType.of(Class.forName("org.apache.http.impl.nio.client.InternalHttpAsyncClient"))));
        assertTrue(target.typeMatcher().matches(TypeDescription.ForLoadedType.of(Class.forName("org.apache.http.impl.nio.client.MinimalHttpAsyncClient"))));
    }

    @Test
    void methodAdvices() {
        assertNotNull(target.methodAdvices());
    }

    @Test
    void onEnter() throws URISyntaxException, HttpException, IOException {
        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class);
            MockedStatic<RepeatedCollectManager> repeatedCollectManager = mockStatic(RepeatedCollectManager.class);
            MockedStatic<FutureCallbackWrapper> futureCallbackWrapper = mockStatic(FutureCallbackWrapper.class);
            MockedStatic<IgnoreUtils> ignoreUtils = mockStatic(IgnoreUtils.class)) {
            // test ignore request
            ignoreUtils.when(() -> IgnoreUtils.excludeOperation(any())).thenReturn(true);
            HttpUriRequest request2 = Mockito.mock(HttpUriRequest.class);
            Mockito.when(request2.getURI()).thenReturn(new URI("http://localhost"));
            HttpAsyncRequestProducer producer = HttpAsyncMethods.create(request2);
            boolean actualResult = InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onEnter(producer, null, null);
            assertFalse(actualResult);

            // test need record
            repeatedCollectManager.when(RepeatedCollectManager::validate).thenReturn(true);
            contextManager.when(ContextManager::needRecordOrReplay).thenReturn(true);
            ignoreUtils.when(() -> IgnoreUtils.excludeOperation(any())).thenReturn(false);
            contextManager.when(ContextManager::needRecord).thenReturn(true);
            FutureCallbackWrapper wrapper = Mockito.mock(FutureCallbackWrapper.class);
            Mockito.when(FutureCallbackWrapper.wrap(any())).thenReturn(wrapper);
            Mockito.when(FutureCallbackWrapper.wrap(any(), any())).thenReturn(wrapper);
            actualResult = InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onEnter(producer, null, null);
            assertFalse(actualResult);

            // test need replay
            repeatedCollectManager.when(RepeatedCollectManager::validate).thenReturn(true);
            contextManager.when(ContextManager::needRecord).thenReturn(false);
            contextManager.when(ContextManager::needReplay).thenReturn(true);
            Mockito.when(FutureCallbackWrapper.wrap(any(), any())).thenReturn(wrapper);
            Mockito.when(wrapper.replay()).thenReturn(MockResult.success("mock"));

            actualResult = InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onEnter(producer, null, null);
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
