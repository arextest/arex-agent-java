package io.arex.inst.httpclient.ning;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.uri.Uri;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.listener.DirectExecutor;
import io.arex.inst.runtime.util.IgnoreUtils;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class AsyncHttpClientInstrumentationTest {
    private static AsyncHttpClientInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new AsyncHttpClientInstrumentation();
        Mockito.mockStatic(IgnoreUtils.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertTrue(target.typeMatcher().matches(TypeDescription.ForLoadedType.of(AsyncHttpClient.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(1, target.methodAdvices().size());
    }

    @Test
    void onEnter() {
        Request mockRequest = Mockito.mock(Request.class);
        Uri uriMock = Mockito.mock(Uri.class);
        MockResult mockResult = MockResult.success("testMockResult");
        Mockito.when(mockRequest.getUri()).thenReturn(uriMock);
        // match ignore
        Mockito.when(IgnoreUtils.excludeOperation(Mockito.any())).thenReturn(true);
        assertFalse(AsyncHttpClientInstrumentation.ExecuteRequestAdvice.onEnter(mockRequest, null, null));
        // not match ignore
        Mockito.when(IgnoreUtils.excludeOperation(Mockito.any())).thenReturn(false);
        // record
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(false);
        assertFalse(AsyncHttpClientInstrumentation.ExecuteRequestAdvice.onEnter(mockRequest, null, null));
        // replay
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(ContextManager.needRecord()).thenReturn(false);
        try (MockedConstruction<HttpClientExtractor> mockedConstruction = Mockito.mockConstruction(HttpClientExtractor.class,
                (mock, context) -> Mockito.doReturn(mockResult).when(mock).replay())) {
            assertTrue(AsyncHttpClientInstrumentation.ExecuteRequestAdvice.onEnter(mockRequest, null, null));
        }
    }

    @Test
    void onExit() {
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        // replay result
        MockResult mockResult = MockResult.success("testMockResult");
        HttpClientExtractor extractorMock = Mockito.mock(HttpClientExtractor.class);
        AsyncHttpClientInstrumentation.ExecuteRequestAdvice.onExit( mockResult, extractorMock, null, null);
        Mockito.verify(extractorMock, Mockito.never()).record(any());
        // replay throwable
        mockResult = MockResult.success(new RuntimeException());
        AsyncHttpClientInstrumentation.ExecuteRequestAdvice.onExit( mockResult, extractorMock, null, null);
        Mockito.verify(extractorMock, Mockito.never()).record(any());

        ListenableFuture listenableFutureMock = Mockito.mock(ListenableFuture.class);
        // record future
        try (MockedConstruction<ResponseFutureListener> mockedConstruction = Mockito.mockConstruction(ResponseFutureListener.class)) {
            AsyncHttpClientInstrumentation.ExecuteRequestAdvice.onExit(null, extractorMock, listenableFutureMock, null);
            Mockito.verify(listenableFutureMock, Mockito.times(1)).addListener(mockedConstruction.constructed().get(0), DirectExecutor.INSTANCE);
        }

        // record throwable
        RuntimeException runtimeException = new RuntimeException("test");
        AsyncHttpClientInstrumentation.ExecuteRequestAdvice.onExit(null, extractorMock, listenableFutureMock, runtimeException);
        Mockito.verify(extractorMock, Mockito.times(1)).record(runtimeException);
    }

}
