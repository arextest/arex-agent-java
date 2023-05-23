package io.arex.foundation.util;

import io.arex.agent.bootstrap.util.StringUtil;
import java.util.concurrent.CompletableFuture;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AsyncHttpClientUtilTest {
    private static CloseableHttpAsyncClient mockClient;
    private static int count = 0;
    @BeforeAll
    static void setUp() throws Exception {
        final Field asyncClient = AsyncHttpClientUtil.class.getDeclaredField("asyncClient");
        asyncClient.setAccessible(true);
        mockClient = Mockito.mock(CloseableHttpAsyncClient.class);
        asyncClient.set(null, mockClient);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
        count = 0;
    }

    @Test
    void executeAsyncIncludeHeader() {
        count++;
        Map<String, String> header = Collections.singletonMap("Content-Type", "application/json");
        final ArgumentCaptor<HttpUriRequest> argumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        AsyncHttpClientUtil.executeAsyncIncludeHeader("mock1", "mock", header);
        Mockito.verify(mockClient, Mockito.times(count)).execute(argumentCaptor.capture(), any());
        assertEquals("mock1", argumentCaptor.getValue().getURI().toString());
    }

    @Test
    void executeAsync() {
        HttpEntity mock = Mockito.mock(HttpEntity.class);
        final Map<String, String> map = Collections.singletonMap("Content-Type", "application/json");
        // more than 5M
        Mockito.when(mock.getContentLength()).thenReturn(6 * 1024 * 1024L);
        final CompletableFuture<String> mock2 = AsyncHttpClientUtil.executeAsync("mock2", mock,
                map);
        final String join = mock2.join();
        assertEquals(StringUtil.EMPTY, join);
        Mockito.verify(mockClient, Mockito.times(count)).execute(any(), any());

        // content length < 0
        Mockito.when(mock.getContentLength()).thenReturn(-1L);
        AsyncHttpClientUtil.executeAsync("mock2", mock, map);
        Mockito.verify(mockClient, Mockito.times(count)).execute(any(), any());

        // less than 5M
        count++;
        final ArgumentCaptor<HttpUriRequest> argumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        Mockito.when(mock.getContentLength()).thenReturn(4 * 1024 * 1024L);
        AsyncHttpClientUtil.executeAsync("mock2", mock, map);
        Mockito.verify(mockClient, Mockito.times(count)).execute(argumentCaptor.capture(), any());
        assertEquals("mock2", argumentCaptor.getValue().getURI().toString());

    }

    @Test
    void executeAsyncTwoStringParam() {
        count++;
        final ArgumentCaptor<HttpUriRequest> argumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        AsyncHttpClientUtil.executeAsync("mock3", "mock");
        Mockito.verify(mockClient, Mockito.times(count)).execute(argumentCaptor.capture(), any());
        assertEquals("mock3", argumentCaptor.getValue().getURI().toString());
    }
}