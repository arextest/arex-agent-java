package io.arex.inst.httpclient.common;

import io.arex.foundation.model.HttpClientMocker;
import io.arex.foundation.serializer.SerializeUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class HttpClientExtractorTest {
    static HttpClientAdapter<?, Object> adapter;
    @InjectMocks
    private HttpClientExtractor<?, Object> httpClientExtractor;
    HttpClientMocker httpClientMocker;

    @BeforeAll
    public static void beforeClass() throws Exception {
        adapter = Mockito.mock(HttpClientAdapter.class);
        when(adapter.getUri()).thenReturn(new URI("http://localhost"));
        Mockito.mockStatic(SerializeUtils.class);
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
        try (MockedConstruction<HttpClientMocker> mocked = Mockito.mockConstruction(HttpClientMocker.class, (mock, context) -> {
            httpClientMocker = mock;
        })){
            when(adapter.wrap(any())).thenReturn(new HttpResponseWrapper());
            httpClientExtractor.record(new Object());
            verify(httpClientMocker).record();
        }
    }

    @Test
    void recordExceptionTest() {
        try (MockedConstruction<HttpClientMocker> mocked = Mockito.mockConstruction(HttpClientMocker.class, (mock, context) -> {
            httpClientMocker = mock;
        })){
            httpClientExtractor.record(new IOException("Connection timeout"));
            verify(httpClientMocker).record();
        }
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
        try (MockedConstruction<HttpClientMocker> mocked = Mockito.mockConstruction(HttpClientMocker.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(new HttpResponseWrapper());
        })) {
            when(adapter.getMethod()).thenReturn("POST");
            when(adapter.getRequestBytes()).thenReturn("mock request".getBytes());
            HttpResponseWrapper wrapper = httpClientExtractor.fetchMockResult();
            assertFalse(wrapper.isIgnoreMockResult());
        }
    }
}