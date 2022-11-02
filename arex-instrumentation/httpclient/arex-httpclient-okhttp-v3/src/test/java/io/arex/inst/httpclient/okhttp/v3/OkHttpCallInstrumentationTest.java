package io.arex.inst.httpclient.okhttp.v3;

import io.arex.foundation.context.ContextManager;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.List;
import org.mockito.junit.jupiter.MockitoExtension;

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
        HttpClientExtractor<Request, Response> extractor = null;
        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class)) {
            contextManager.when(ContextManager::needRecordOrReplay).thenReturn(true);
            boolean actResult = OkHttpCallInstrumentation.ExecuteAdvice.onEnter(call, extractor);
            Assertions.assertFalse(actResult);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void executeAdviceExitTest() throws Exception {
        HttpClientExtractor<Request, Response> extractor = mock(HttpClientExtractor.class);
        Exception throwable = new IOException("Not found");
        Response response;
        OkHttpCallInstrumentation.ExecuteAdvice.onExit(extractor, null, null);
        verify(extractor, never()).replay();
        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class)) {
            contextManager.when(ContextManager::needReplay).thenReturn(true);
            OkHttpCallInstrumentation.ExecuteAdvice.onExit(extractor, null, null);
            verify(extractor).replay();
        }
        response = OkHttpCallbackWrapperTest.createResponse();
        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class)) {
            contextManager.when(ContextManager::needReplay).thenReturn(false);
            OkHttpCallInstrumentation.ExecuteAdvice.onExit(extractor, null, response);
            verify(extractor).record(response);
            OkHttpCallInstrumentation.ExecuteAdvice.onExit(extractor, throwable, response);
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