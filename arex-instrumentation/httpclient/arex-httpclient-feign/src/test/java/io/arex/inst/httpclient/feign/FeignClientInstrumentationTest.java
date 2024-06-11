package io.arex.inst.httpclient.feign;

import static org.junit.jupiter.api.Assertions.*;

import feign.Request;
import feign.Response;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.util.IgnoreUtils;
import java.util.HashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FeignClientInstrumentationTest {
    private static FeignClientInstrumentation clientInstrumentation;

    @BeforeAll
    static void setUp() {
        clientInstrumentation = new FeignClientInstrumentation();
        Mockito.mockStatic(IgnoreUtils.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
    }

    @AfterAll
    static void tearDown() {
        clientInstrumentation = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertDoesNotThrow(() -> clientInstrumentation.typeMatcher());
    }

    @Test
    void methodAdvices() {
        assertEquals(1, clientInstrumentation.methodAdvices().size());
        assertEquals("io.arex.inst.httpclient.feign.FeignClientInstrumentation$ExecuteAdvice",
                clientInstrumentation.methodAdvices().get(0).getAdviceClassName());
    }

    @Test
    void onEnter() {
        // not need record or replay
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);
        assertFalse(FeignClientInstrumentation.ExecuteAdvice.onEnter(null, null, null, null));

        // need record or replay but exclude operation
        String url = "http://localhost:8080/test";
        final Request request = Request.create("post", "http://localhost:8080/test", new HashMap<>(), null, null);
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(IgnoreUtils.excludeOperation(url)).thenReturn(true);
        assertFalse(FeignClientInstrumentation.ExecuteAdvice.onEnter(request, null, null, null));

        // need record and not exclude operation
        Mockito.when(IgnoreUtils.excludeOperation(url)).thenReturn(false);
        Mockito.when(ContextManager.needReplay()).thenReturn(false);
        assertFalse(FeignClientInstrumentation.ExecuteAdvice.onEnter(request, null, null, null));

        // need replay and not exclude operation
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        assertTrue(FeignClientInstrumentation.ExecuteAdvice.onEnter(request, null, null, null));
    }

    @Test
    void onExit() {
        MockResult mockResult = Mockito.mock(MockResult.class);
        final HttpClientExtractor clientExtractor = Mockito.mock(HttpClientExtractor.class);
        // extractor is null
        FeignClientInstrumentation.ExecuteAdvice.onExit(null,null, null, null, null);
        Mockito.verify(mockResult, Mockito.never()).notIgnoreMockResult();

        // extractor is not null but mockResult is null
        FeignClientInstrumentation.ExecuteAdvice.onExit(null, clientExtractor, null, null, null);
        Mockito.verify(mockResult, Mockito.never()).notIgnoreMockResult();

        // extractor is not null and mockResult is not null and mockResult.getThrowable() is not null
        Mockito.when(mockResult.getThrowable()).thenReturn(new NullPointerException());
        Mockito.when(mockResult.notIgnoreMockResult()).thenReturn(true);
        FeignClientInstrumentation.ExecuteAdvice.onExit(null, clientExtractor, mockResult, null, null);
        Mockito.verify(mockResult, Mockito.never()).getResult();

        // extractor is not null and mockResult is not null and mockResult.getThrowable() is null
        Mockito.when(mockResult.getThrowable()).thenReturn(null);
        FeignClientInstrumentation.ExecuteAdvice.onExit(null, clientExtractor, mockResult, null, null);
        Mockito.verify(mockResult, Mockito.times(1)).getResult();

        // record exception
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        final FeignClientAdapter clientAdapter = Mockito.mock(FeignClientAdapter.class);
        final RuntimeException exception = new RuntimeException();
        FeignClientInstrumentation.ExecuteAdvice.onExit(clientAdapter, clientExtractor, null, null, exception);
        Mockito.verify(clientExtractor, Mockito.times(1)).record(exception);

        // record response
        final Response response = Mockito.mock(Response.class);
        FeignClientInstrumentation.ExecuteAdvice.onExit(clientAdapter, clientExtractor, null, response, null);
        Mockito.verify(clientExtractor, Mockito.times(1)).record(Mockito.any());
    }
}
