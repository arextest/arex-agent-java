package io.arex.foundation.util.httpclient;

import io.arex.foundation.model.HttpClientResponse;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AsyncHttpClientUtilTest {
    private static CloseableHttpAsyncClient mockAsyncClient;
    @BeforeAll
    static void setUp() throws Exception {
        final Field asyncClient = AsyncHttpClientUtil.class.getDeclaredField("asyncClient");
        asyncClient.setAccessible(true);
        mockAsyncClient = Mockito.mock(CloseableHttpAsyncClient.class);
        asyncClient.set(null, mockAsyncClient);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void postAsyncWithJson() {
        Map<String, String> mockHeaders = new HashMap<>();
        mockHeaders.put("If-Modified-Since", "1689770421677");

        String uri = "http://localhost:8080/agentStatus";
        CompletableFuture<HttpClientResponse> responseFuture = AsyncHttpClientUtil.postAsyncWithJson(uri, "", mockHeaders);

        final ArgumentCaptor<HttpUriRequest> argumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        Mockito.verify(mockAsyncClient, Mockito.atLeastOnce()).execute(argumentCaptor.capture(), any());
        assertEquals(uri, argumentCaptor.getValue().getURI().toString());
    }

    @Test
    void postAsyncWithZstdJson() {
        String json = "{\n"
            + "    \"categoryType\": {\n"
            + "        \"name\": \"DynamicClass\",\n"
            + "        \"entryPoint\": false,\n"
            + "        \"skipComparison\": true\n"
            + "    },\n"
            + "    \"replayId\": \"AREX-10-130-218-147-19739174106252\",\n"
            + "    \"recordId\": \"AREX-172-26-0-4-78802751936100\",\n"
            + "    \"appId\": \"community-test-0720\",\n"
            + "    \"recordEnvironment\": 0,\n"
            + "    \"recordVersion\": \"0.3.6\",\n"
            + "    \"creationTime\": 1689700307421,\n"
            + "    \"targetRequest\": {\n"
            + "        \"body\": null,\n"
            + "        \"attributes\": null,\n"
            + "        \"type\": null\n"
            + "    },\n"
            + "    \"targetResponse\": {\n"
            + "        \"body\": null,\n"
            + "        \"attributes\": null,\n"
            + "        \"type\": null\n"
            + "    },\n"
            + "    \"operationName\": \"java.lang.System.currentTimeMillis\"\n"
            + "}";
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-AREX-Mock-Strategy-Code", "0");

        String uri = "http://localhost/api/storage/record/query";

        CompletableFuture<HttpClientResponse> responseFuture = AsyncHttpClientUtil.postAsyncWithZstdJson(uri, json, requestHeaders);

        final ArgumentCaptor<HttpUriRequest> argumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        Mockito.verify(mockAsyncClient, Mockito.atLeastOnce()).execute(argumentCaptor.capture(), any());
        assertEquals(uri, argumentCaptor.getValue().getURI().toString());
    }


    @Test
    void executeAsync() {
        HttpEntity mockHttpEntity = Mockito.mock(HttpEntity.class);
        final Map<String, String> mockHeaders = Collections.singletonMap("Content-Type", "application/json");
        // more than 5M
        Mockito.when(mockHttpEntity.getContentLength()).thenReturn(6 * 1024 * 1024L);
        CompletableFuture<HttpClientResponse> responseFuture = AsyncHttpClientUtil.executeAsync("max.body.limit", mockHttpEntity,
                mockHeaders, null);
        HttpClientResponse actualResult = responseFuture.join();
        assertNull(actualResult.getBody());

        // content length < 0
        Mockito.when(mockHttpEntity.getContentLength()).thenReturn(-1L);
        responseFuture = AsyncHttpClientUtil.executeAsync("content.length.less.than.zero", mockHttpEntity,
            mockHeaders, null);
        actualResult = responseFuture.join();
        assertNull(actualResult.getBody());

        // less than 5M
        final ArgumentCaptor<HttpUriRequest> argumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        Mockito.when(mockHttpEntity.getContentLength()).thenReturn(4 * 1024 * 1024L);
        responseFuture = AsyncHttpClientUtil.executeAsync("normal.content.length", mockHttpEntity,
mockHeaders, null);
        Mockito.verify(mockAsyncClient, Mockito.atLeastOnce()).execute(argumentCaptor.capture(), any());
        assertEquals("normal.content.length", argumentCaptor.getValue().getURI().toString());
    }
}
