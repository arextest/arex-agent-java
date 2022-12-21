package io.arex.inst.httpclient.common;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import java.io.IOException;
import java.net.URI;
import org.junit.jupiter.api.AfterAll;
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
    }

    @AfterAll
    static void tearDown() {
        adapter = null;
        Mockito.clearAllCaches();
    }

    @Test
    void recordResponseTest() {
        try (MockedStatic<MockUtils> mockUtils = mockStatic(MockUtils.class)) {
            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            mockUtils.when(() -> MockUtils.createHttpClient(any())).thenReturn(mocker);

            assertDoesNotThrow(() -> {
                httpClientExtractor.record(new Object());
            });
        }
    }

    @Test
    void recordExceptionTest() {
        try (MockedStatic<MockUtils> mockUtils = mockStatic(MockUtils.class)) {
            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            mockUtils.when(() -> MockUtils.createHttpClient(any())).thenReturn(mocker);
            assertDoesNotThrow(() -> {
                httpClientExtractor.record(new IOException("Connection timeout"));
            });
        }
    }

    @Test
    void replay() {
        try (MockedStatic<MockUtils> mockUtils = mockStatic(MockUtils.class);
            MockedStatic<IgnoreUtils> ignoreService = mockStatic(IgnoreUtils.class)) {
            ignoreService.when(() -> IgnoreUtils.ignoreMockResult(any(), any())).thenReturn(true);

            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            mockUtils.when(() -> MockUtils.createHttpClient(any())).thenReturn(mocker);
            mockUtils.when(() -> MockUtils.replayBody(any())).thenReturn(new HttpResponseWrapper());
            when(adapter.unwrap(any())).thenReturn(new Object());
            // replay success
            MockResult mockResult = httpClientExtractor.replay();
            assertNotNull(mockResult.getResult());

            // replay exception
            mockUtils.when(() -> MockUtils.replayBody(any())).thenReturn(new RuntimeException(""));

            mockResult = httpClientExtractor.replay();
            assertNotNull(mockResult.getThrowable());

            // replay null
            mockUtils.when(() -> MockUtils.replayBody(any())).thenReturn(new Object());
            mockResult = httpClientExtractor.replay();
            assertNull(mockResult);
        }
    }
}