package io.arex.inst.httpclient.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import java.io.IOException;
import java.net.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class HttpClientExtractorTest {
    static HttpClientAdapter<?, Object> adapter;
    @InjectMocks
    private HttpClientExtractor<?, Object> httpClientExtractor;

    @BeforeAll
    public static void beforeClass() throws Exception {
        adapter = Mockito.mock(HttpClientAdapter.class);

        when(adapter.getUri()).thenReturn(new URI("http://localhost"));
        when(adapter.getMethod()).thenReturn("POST");
        when(adapter.getRequestBytes()).thenReturn("mock request".getBytes());
        when(adapter.getRequestContentType()).thenReturn("application/json");
        when(adapter.wrap(any())).thenReturn(new HttpResponseWrapper());

        Mockito.mockStatic(Serializer.class);
    }

    @AfterAll
    static void tearDown() {
        adapter = null;
        Mockito.clearAllCaches();
    }

    @Test
    public void recordNullTest() {
        HttpResponseWrapper wrapper = null;
        httpClientExtractor.record(wrapper);
        assertNull(wrapper);
    }

    @Test
    public void recordResponseTest() {
        httpClientExtractor.record(new Object());
    }

    @Test
    void recordExceptionTest() {
        httpClientExtractor.record(new IOException("Connection timeout"));
    }

    @Test
    public void replayNullTest() {
        Assertions.assertThrows(ArexDataException.class, () -> {
            httpClientExtractor.replay(null);
        });
    }

    @Test
    public void replayExceptionTest() {
        Assertions.assertThrows(ArexDataException.class, () -> {
            HttpResponseWrapper wrapped = new HttpResponseWrapper();
            wrapped.setException(new ExceptionWrapper(null));
            httpClientExtractor.replay(wrapped);
        });
    }

    @Test
    void fetchMockResult() {
        try (MockedStatic<MockUtils> mockService = mockStatic(MockUtils.class);
            MockedStatic<IgnoreUtils> ignoreService = mockStatic(IgnoreUtils.class)) {
            ignoreService.when(()-> IgnoreUtils.ignoreMockResult(any(), any())).thenReturn(true);

            HttpResponseWrapper responseWrapper = new HttpResponseWrapper();
            mockService.when(()->  MockUtils.replayBody(any())).thenReturn(responseWrapper);

            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            mockService.when(()-> MockUtils.createHttpClient(any())).thenReturn(mocker);

            HttpResponseWrapper wrapper = httpClientExtractor.fetchMockResult();
            assertEquals(responseWrapper, wrapper);;
            assertTrue(wrapper.isIgnoreMockResult());
        }
    }
}