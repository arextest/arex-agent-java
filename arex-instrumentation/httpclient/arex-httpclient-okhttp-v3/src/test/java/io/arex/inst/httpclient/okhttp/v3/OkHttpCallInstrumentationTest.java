package io.arex.inst.httpclient.okhttp.v3;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.List;

import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OkHttpCallInstrumentationTest {
    @InjectMocks
    private OkHttpCallInstrumentation okHttpCallInstrumentation;

    @Test
    public void methodAdvicesTest() {
        List<?> actResult = okHttpCallInstrumentation.methodAdvices();
        Assertions.assertNotNull(actResult);
        Assertions.assertEquals(2, actResult.size());
    }

    @Test
    public void executeAdviceEnterTest() {
        Call call = mock(Call.class);
        when(call.request()).thenReturn(OkHttpCallbackWrapperTest.createRequest());
        Mockito.mockConstruction(HttpClientExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success("mock"));
        });
        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class)) {
            contextManager.when(ContextManager::needRecordOrReplay).thenReturn(true);
            contextManager.when(ContextManager::needReplay).thenReturn(true);
            boolean actResult = OkHttpCallInstrumentation.ExecuteAdvice.onEnter(call, null, null);
            Assertions.assertTrue(actResult);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void executeAdviceExitTest() throws Exception {
        HttpClientExtractor<Request, MockResult> extractor = mock(HttpClientExtractor.class);
        Exception throwable = new IOException("Not found");
        Response response = OkHttpCallbackWrapperTest.createResponse();
        OkHttpCallInstrumentation.ExecuteAdvice.onExit(null, null, null, null);
        verify(extractor, never()).replay();
        MockResult mockResult = MockResult.success(response);
        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class)) {
            contextManager.when(ContextManager::needReplay).thenReturn(true);
            OkHttpCallInstrumentation.ExecuteAdvice.onExit(extractor, null, null, mockResult);
            verify(extractor, never()).replay();
        }
        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class)) {
            contextManager.when(ContextManager::needReplay).thenReturn(false);
            contextManager.when(ContextManager::needRecord).thenReturn(true);
            OkHttpCallInstrumentation.ExecuteAdvice.onExit(extractor, null, response, null);
            verify(extractor).record(any(MockResult.class));
            OkHttpCallInstrumentation.ExecuteAdvice.onExit(extractor, throwable, response, null);
            verify(extractor).record(throwable);
        }
    }

    @Test
    public void enqueueAdviceEnterTest() {
        Call call = mock(Call.class);
        when(call.request()).thenReturn(OkHttpCallbackWrapperTest.createRequest());
        Callback callback = mock(Callback.class);
        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class)) {
            contextManager.when(ContextManager::needRecordOrReplay).thenReturn(true);
            boolean actResult = OkHttpCallInstrumentation.EnqueueAdvice.onEnter(call, callback);
            Assertions.assertFalse(actResult);
        }
    }

    @Test
    public void enqueueAdviceExitTest() throws Exception {
        Callback callback = mock(Callback.class);
        OkHttpCallInstrumentation.EnqueueAdvice.onExit(callback);
        OkHttpCallbackWrapper callbackWrapper = mock(OkHttpCallbackWrapper.class);
        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class)) {
            contextManager.when(ContextManager::needReplay).thenReturn(false, true);
            OkHttpCallInstrumentation.EnqueueAdvice.onExit(callbackWrapper);
            verify(callbackWrapper, never()).replay();
            OkHttpCallInstrumentation.EnqueueAdvice.onExit(callbackWrapper);
            verify(callbackWrapper).replay();
        }
    }
}