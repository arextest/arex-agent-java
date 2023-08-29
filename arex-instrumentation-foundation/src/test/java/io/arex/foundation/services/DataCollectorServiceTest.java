package io.arex.foundation.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.internal.DataEntity;
import io.arex.foundation.model.HttpClientResponse;
import io.arex.foundation.util.httpclient.AsyncHttpClientUtil;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataCollectorServiceTest {
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(AsyncHttpClientUtil.class);
        Mockito.mockStatic(HealthManager.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void saveData() {
        CompletableFuture<HttpClientResponse> mockResponse = CompletableFuture.completedFuture(HttpClientResponse.emptyResponse());
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), anyString(), any())).thenReturn(mockResponse);
        assertDoesNotThrow(()-> DataCollectorService.INSTANCE.saveData(new DataEntity("test")));

        CompletableFuture<HttpClientResponse> mockException = new CompletableFuture<>();
        mockException.completeExceptionally(new RuntimeException("mock exception"));
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), anyString(), any())).thenReturn(mockException);
        assertDoesNotThrow(()-> DataCollectorService.INSTANCE.saveData(new DataEntity("test")));
    }

    @Test
    void queryReplayData() {
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));
        String actualResult = DataCollectorService.INSTANCE.queryReplayData("test", MockStrategyEnum.OVER_BREAK);
        assertNull(actualResult);

        CompletableFuture<HttpClientResponse> mockResponse = CompletableFuture.completedFuture(new HttpClientResponse(200, null, "test"));
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), anyString(), any())).thenReturn(mockResponse);
        actualResult = DataCollectorService.INSTANCE.queryReplayData("test", MockStrategyEnum.OVER_BREAK);
        assertEquals("test", actualResult);
    }
}