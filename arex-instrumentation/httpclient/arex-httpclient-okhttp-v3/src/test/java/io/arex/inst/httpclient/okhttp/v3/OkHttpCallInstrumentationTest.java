package io.arex.inst.httpclient.okhttp.v3;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OkHttpCallInstrumentationTest {
    @InjectMocks
    private OkHttpCallInstrumentation okHttpCallInstrumentation;

    @Test
    void methodAdvicesTest() {
        List<?> actResult = okHttpCallInstrumentation.methodAdvices();
        Assertions.assertNotNull(actResult);
        Assertions.assertEquals(2, actResult.size());
    }

    @Test
    void executeAdviceEnterTest() {
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
    void executeAdviceExitTest() throws Exception {
        HttpClientExtractor<Request, Response> extractor = mock(HttpClientExtractor.class);
        Exception throwable = new IOException("Not found");
        Response response = OkHttpCallbackWrapperTest.createResponse();
        OkHttpCallInstrumentation.ExecuteAdvice.onExit(null, null, null, null);

        OkHttpCallInstrumentation.ExecuteAdvice.onExit(extractor, null, null, MockResult.success(response));
        OkHttpCallInstrumentation.ExecuteAdvice.onExit(extractor, null, null, MockResult.success(new RuntimeException()));

        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class)) {
            contextManager.when(ContextManager::needRecord).thenReturn(true);
            OkHttpCallInstrumentation.ExecuteAdvice.onExit(extractor, null, response, null);
            verify(extractor).record(any(Response.class));
            OkHttpCallInstrumentation.ExecuteAdvice.onExit(extractor, throwable, response, null);
            verify(extractor).record(throwable);
        }
    }

    @Test
    void enqueueAdviceEnterTest() {
        Call call = Mockito.mock(Call.class);
        Callback callback = Mockito.mock(Callback.class);
        MockResult mockResult = MockResult.success("mock");
        try (MockedConstruction<OkHttpCallbackWrapper> mocked = Mockito.mockConstruction(OkHttpCallbackWrapper.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(mockResult);
        })) {
            try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class);
                MockedStatic<RepeatedCollectManager> repeatedCollectManager = mockStatic(RepeatedCollectManager.class)) {
                contextManager.when(ContextManager::needRecordOrReplay).thenReturn(false);
                boolean actResult = OkHttpCallInstrumentation.EnqueueAdvice.onEnter(call, callback, mockResult);
                Assertions.assertFalse(actResult);

                repeatedCollectManager.when(RepeatedCollectManager::validate).thenReturn(true);
                contextManager.when(ContextManager::needRecordOrReplay).thenReturn(true);
                contextManager.when(ContextManager::needReplay).thenReturn(true);

                actResult = OkHttpCallInstrumentation.EnqueueAdvice.onEnter(call, callback, mockResult);
                Assertions.assertTrue(actResult);
            }
        }
    }

    @Test
    void enqueueAdviceExitTest() {
        MockResult mockResult = MockResult.success("mock");
        OkHttpCallbackWrapper callbackWrapper = Mockito.mock(OkHttpCallbackWrapper.class);

        OkHttpCallInstrumentation.EnqueueAdvice.onExit(callbackWrapper, mockResult);
        verify(callbackWrapper).replay(mockResult);
    }
}