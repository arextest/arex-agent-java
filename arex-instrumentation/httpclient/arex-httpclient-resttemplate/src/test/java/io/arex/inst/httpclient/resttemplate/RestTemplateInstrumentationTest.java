package io.arex.inst.httpclient.resttemplate;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import java.net.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RequestCallback;

class RestTemplateInstrumentationTest {
    private static RestTemplateInstrumentation restTemplateInstrumentation = null;
    private static URI uri;
    private static RequestCallback requestCallback;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
        restTemplateInstrumentation = new RestTemplateInstrumentation();
        uri = Mockito.mock(URI.class);
        requestCallback = Mockito.mock(RequestCallback.class);
    }

    @AfterAll
    static void tearDown() {
        restTemplateInstrumentation = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertNotNull(restTemplateInstrumentation.typeMatcher());
    }

    @Test
    void methodAdvices() {
        assertNotNull(restTemplateInstrumentation.methodAdvices());
    }

    @Test
    void onEnter() {
        // replay mockResult and not ignore
        try (MockedConstruction<RestTemplateExtractor> mocked = Mockito.mockConstruction(RestTemplateExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success(false, null));
        })) {
            RestTemplateExtractor extractor = null;
            MockResult mockResult = null;
            // not record or replay
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);
            assertFalse(RestTemplateInstrumentation.ExecuteAdvice.onEnter(uri, HttpMethod.POST, requestCallback, null, null));

            // record
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            assertFalse(RestTemplateInstrumentation.ExecuteAdvice.onEnter(uri, HttpMethod.POST, requestCallback, extractor, null));

            // replay
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            assertTrue(RestTemplateInstrumentation.ExecuteAdvice.onEnter(uri, HttpMethod.POST, requestCallback, extractor, mockResult));
        }
    }

    @Test
    void onExit() {
        Object result = "testResult";
        Throwable throwable = null;
        // replay
        final RestTemplateExtractor extractor = Mockito.mock(RestTemplateExtractor.class);
        final MockResult resultWithThrowable = MockResult.success(false, new RuntimeException());
        RestTemplateInstrumentation.ExecuteAdvice.onExit(result, throwable, extractor, resultWithThrowable);
        Mockito.verify(extractor, Mockito.times(0)).record(Mockito.any(), Mockito.any());

        MockResult resultWithoutThrowable = MockResult.success(false, null);
        RestTemplateInstrumentation.ExecuteAdvice.onExit(result, throwable, extractor, resultWithoutThrowable);
        Mockito.verify(extractor, Mockito.times(0)).record(Mockito.any(), Mockito.any());

        // record
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.when(RepeatedCollectManager.exitAndValidate(Mockito.anyString())).thenReturn(true);
        RestTemplateInstrumentation.ExecuteAdvice.onExit(result, throwable, extractor, null);
        Mockito.verify(extractor, Mockito.times(1)).record(result, throwable);

    }
}
