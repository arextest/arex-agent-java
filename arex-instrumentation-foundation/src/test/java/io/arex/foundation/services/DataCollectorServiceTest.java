package io.arex.foundation.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.internal.DataEntity;
import io.arex.foundation.model.DecelerateReasonEnum;
import io.arex.foundation.model.HttpClientResponse;
import io.arex.foundation.serializer.gson.GsonSerializer;
import io.arex.foundation.util.httpclient.AsyncHttpClientUtil;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.QueryAllMockerDTO;
import io.arex.inst.runtime.model.ReplayCompareResultDTO;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.CaseManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class DataCollectorServiceTest {
    static MockedStatic<CaseManager> caseManagerMocked;
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(AsyncHttpClientUtil.class);
        Mockito.mockStatic(HealthManager.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(Serializer.class);
        caseManagerMocked = Mockito.mockStatic(CaseManager.class);
        Mockito.mockStatic(LogManager.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void saveData() {
        final ArexMocker mocker = new ArexMocker();
        mocker.setRecordId("testRecordId");
        CompletableFuture<HttpClientResponse> mockResponse = CompletableFuture.completedFuture(HttpClientResponse.emptyResponse());
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), any(), any())).thenReturn(mockResponse);
        assertDoesNotThrow(()-> DataCollectorService.INSTANCE.saveData(new DataEntity(Collections.singletonList(mocker))));

        CompletableFuture<HttpClientResponse> mockException = new CompletableFuture<>();
        mockException.completeExceptionally(new RuntimeException("mock exception"));
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), any(), any())).thenReturn(mockException);
        assertDoesNotThrow(()-> DataCollectorService.INSTANCE.saveData(new DataEntity(Collections.singletonList(mocker))));
        caseManagerMocked.verify(()-> CaseManager.invalid("testRecordId", null, null, DecelerateReasonEnum.SERVICE_EXCEPTION.getValue()), Mockito.times(1));

        // null entity
        assertDoesNotThrow(()-> DataCollectorService.INSTANCE.saveData(null));

        // invalid case
        final ArexContext context = ArexContext.of("testRecordId");
        context.setInvalidCase(true);
        Mockito.when(ContextManager.getContext("testRecordId")).thenReturn(context);
        mocker.setRecordId("testRecordId");
        assertDoesNotThrow(()-> DataCollectorService.INSTANCE.saveData(new DataEntity(Collections.singletonList(mocker))));
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
        // exception
        ArexMocker mocker = new ArexMocker();
        mocker.setRecordId("testRecordId");
        mocker.setReplayId("testReplayId");
        String json = GsonSerializer.INSTANCE.serialize(mocker);
        CompletableFuture<HttpClientResponse> mockException = new CompletableFuture<>();
        mockException.completeExceptionally(new RuntimeException("mock exception"));
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), any(), any())).thenReturn(mockException);
        Mockito.when(Serializer.deserialize(json, ArexMocker.class)).thenReturn(mocker);
        assertDoesNotThrow(()-> DataCollectorService.INSTANCE.queryReplayData(json, MockStrategyEnum.OVER_BREAK));
        caseManagerMocked.verify(()-> CaseManager.invalid("testRecordId", "testReplayId", null, DecelerateReasonEnum.SERVICE_EXCEPTION.getValue()), Mockito.times(1));
    }

    @Test
    void invalidCase() {
        assertDoesNotThrow(()-> DataCollectorService.INSTANCE.invalidCase("test"));
    }

    @Test
    void start() {
        assertDoesNotThrow(DataCollectorService.INSTANCE::start);
    }

    @Test
    void save() {
        ConfigManager.INSTANCE.setBufferSize("1");
        DataCollectorService.INSTANCE.start();
        ArexMocker mocker = new ArexMocker();
        // first add max buffer size
        DataCollectorService.INSTANCE.save(Collections.singletonList(mocker));
        assertDoesNotThrow(()-> DataCollectorService.INSTANCE.save(Collections.singletonList(mocker)));
    }

    @Test
    void queryAll() {
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        String actualResult = DataCollectorService.INSTANCE.queryAll("test");
        assertNull(actualResult);

        CompletableFuture<HttpClientResponse> mockResponse =
                CompletableFuture.completedFuture(new HttpClientResponse(200, null, "test"));
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), anyString(), any())).thenReturn(mockResponse);
        actualResult = DataCollectorService.INSTANCE.queryAll("test");
        assertEquals("test", actualResult);
        // exception
        String json = "{\"recordId\":\"testRecordId\",\"replayId\":\"testReplayId\"}";
        CompletableFuture<HttpClientResponse> mockException = new CompletableFuture<>();
        mockException.completeExceptionally(new RuntimeException("mock exception"));
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), any(), any())).thenReturn(mockException);
        Mockito.when(Serializer.deserialize(json, QueryAllMockerDTO.class)).thenReturn(new QueryAllMockerDTO());
        assertDoesNotThrow(()-> DataCollectorService.INSTANCE.queryAll(json));
    }

    @Test
    void order() {
        assertEquals(0, DataCollectorService.INSTANCE.order());
    }

    @Test
    void saveReplayCompareResult() {
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        assertDoesNotThrow(() -> DataCollectorService.INSTANCE.saveReplayCompareResult("mock"));
        // exception
        CompletableFuture<HttpClientResponse> mockException = new CompletableFuture<>();
        mockException.completeExceptionally(new RuntimeException("mock exception"));
        Mockito.when(AsyncHttpClientUtil.postAsyncWithZstdJson(anyString(), any(), any())).thenReturn(mockException);
        Mockito.when(Serializer.deserialize(any(), eq(ArexConstants.REPLAY_COMPARE_TYPE))).
                thenReturn(Collections.singletonList(new ReplayCompareResultDTO()));
        assertDoesNotThrow(() -> DataCollectorService.INSTANCE.saveReplayCompareResult("mock"));
    }
}
