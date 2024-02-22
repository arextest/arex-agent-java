package io.arex.foundation.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.model.AgentStatusEnum;
import io.arex.foundation.model.AgentStatusRequest;
import io.arex.foundation.model.ConfigQueryRequest;
import io.arex.foundation.model.ConfigQueryResponse;
import io.arex.foundation.model.ConfigQueryResponse.ResponseBody;
import io.arex.foundation.model.ConfigQueryResponse.ServiceCollectConfig;
import io.arex.foundation.model.HttpClientResponse;
import io.arex.foundation.serializer.jackson.JacksonSerializer;
import io.arex.foundation.util.NetUtils;
import io.arex.foundation.util.httpclient.AsyncHttpClientUtil;
import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.MockedStatic;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigServiceTest {

    @BeforeEach
    void setUp() {
        System.setProperty("arex.another.property", "test");
        System.setProperty("arex.tags.env", "fat");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("arex.another.property");
        System.clearProperty("arex.tags.env");
    }

    @Order(1)
    @Test
    void loadAgentConfig() throws Throwable {
        long DELAY_MINUTES = 15L;
        // local
        long actualResult = ConfigService.INSTANCE.loadAgentConfig("arex.service.name=unit-test-service;arex.enable.debug=true;arex.storage.mode=local");
        assertEquals(-1, actualResult);

        // not local
        ConfigManager.INSTANCE.setStorageServiceMode("local");
        assertEquals(-1, ConfigService.INSTANCE.loadAgentConfig(null));

        ConfigManager.INSTANCE.setServiceName("config-test");
        ConfigManager.INSTANCE.setStorageServiceMode("mock-not-local");
        try (MockedStatic<AsyncHttpClientUtil> ahc = mockStatic(AsyncHttpClientUtil.class);
            MockedStatic<NetUtils> netUtils = mockStatic(NetUtils.class)){
            // response has error
            CompletableFuture<HttpClientResponse> errorFuture = new CompletableFuture<>();
            errorFuture.completeExceptionally(new RuntimeException("mock error"));
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), anyMap())).thenReturn(errorFuture);
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertEquals(0, ConfigManager.INSTANCE.getRecordRate());
            assertEquals(EnumSet.noneOf(DayOfWeek.class), ConfigManager.INSTANCE.getAllowDayOfWeeks());
            assertEquals(AgentStatusEnum.UN_START, ConfigService.INSTANCE.getAgentStatus());

            // clientResponse is null
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), eq(null))).thenReturn(CompletableFuture.completedFuture(null));
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertEquals(0, ConfigManager.INSTANCE.getRecordRate());
            assertEquals(EnumSet.noneOf(DayOfWeek.class), ConfigManager.INSTANCE.getAllowDayOfWeeks());
            assertEquals(AgentStatusEnum.UN_START, ConfigService.INSTANCE.getAgentStatus());

            // clientResponse body is null
            netUtils.when(NetUtils::getIpAddress).thenReturn("127.0.0.3");
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), eq(null))).thenReturn(
                CompletableFuture.completedFuture(HttpClientResponse.emptyResponse()));
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertEquals(0, ConfigManager.INSTANCE.getRecordRate());
            assertEquals(EnumSet.noneOf(DayOfWeek.class), ConfigManager.INSTANCE.getAllowDayOfWeeks());
            assertEquals(AgentStatusEnum.UN_START, ConfigService.INSTANCE.getAgentStatus());

            // configResponse body serviceCollectConfiguration is null
            ConfigQueryResponse configQueryResponse = new ConfigQueryResponse();
            netUtils.when(NetUtils::getIpAddress).thenReturn("127.0.0.3");
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), eq(null))).thenReturn(
                CompletableFuture.completedFuture(new HttpClientResponse(200, null, JacksonSerializer.INSTANCE.serialize(configQueryResponse))));
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertEquals(0, ConfigManager.INSTANCE.getRecordRate());
            assertEquals(EnumSet.noneOf(DayOfWeek.class), ConfigManager.INSTANCE.getAllowDayOfWeeks());
            assertEquals(AgentStatusEnum.UN_START, ConfigService.INSTANCE.getAgentStatus());

            // valid response, agentStatus=WORKING
            netUtils.when(NetUtils::getIpAddress).thenReturn("127.0.0.1");
            ServiceCollectConfig serviceCollectConfig = new ServiceCollectConfig();
            serviceCollectConfig.setAllowDayOfWeeks(127);
            serviceCollectConfig.setAllowTimeOfDayFrom("00:00");
            serviceCollectConfig.setAllowTimeOfDayTo("23:59");
            serviceCollectConfig.setSampleRate(1);

            ResponseBody responseBody = new ResponseBody();
            responseBody.setTargetAddress("127.0.0.1");
            responseBody.setServiceCollectConfiguration(serviceCollectConfig);
            configQueryResponse.setBody(responseBody);
            CompletableFuture<HttpClientResponse> response = CompletableFuture.completedFuture(new HttpClientResponse(200, null, JacksonSerializer.INSTANCE.serialize(configQueryResponse)));
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), eq(null))).thenReturn(response);
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertTrue(ConfigManager.INSTANCE.inWorkingTime() && ConfigManager.INSTANCE.getRecordRate() > 0);
            ConfigManager.FIRST_TRANSFORM.compareAndSet(false, true);
            assertEquals(AgentStatusEnum.WORKING, ConfigService.INSTANCE.getAgentStatus());


            // valid response, agentStatus=SLEEPING
            serviceCollectConfig.setAllowDayOfWeeks(0);
            response = CompletableFuture.completedFuture(
                new HttpClientResponse(200, null, JacksonSerializer.INSTANCE.serialize(configQueryResponse)));
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), eq(null))).thenReturn(response);
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertFalse(ConfigManager.INSTANCE.inWorkingTime());
            assertEquals(AgentStatusEnum.SLEEPING, ConfigService.INSTANCE.getAgentStatus());

            // valid response, agentStatus=SLEEPING
            serviceCollectConfig.setAllowDayOfWeeks(127);
            response = CompletableFuture.completedFuture(
                new HttpClientResponse(200, null, JacksonSerializer.INSTANCE.serialize(configQueryResponse)));
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), eq(null))).thenReturn(response);
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertTrue(ConfigManager.INSTANCE.inWorkingTime() && ConfigManager.INSTANCE.getRecordRate() > 0);
            assertEquals(AgentStatusEnum.WORKING, ConfigService.INSTANCE.getAgentStatus());
        }
    }

    @Test
    void serialize() {
        assertNull(ConfigService.INSTANCE.serialize(null));
        assertNull(ConfigService.INSTANCE.deserialize(null, null));

        AgentStatusRequest expectRequest = new AgentStatusRequest("appid", "ip", "agentStatus");
        String expectJson = ConfigService.INSTANCE.serialize(expectRequest);
        AgentStatusRequest actualRequest = ConfigService.INSTANCE.deserialize(expectJson, AgentStatusRequest.class);
        String actualJson = ConfigService.INSTANCE.serialize(actualRequest);
        assertEquals(expectJson, actualJson);
    }

    @Test
    void reportStatus() {
        try (MockedStatic<AsyncHttpClientUtil> ahc = mockStatic(AsyncHttpClientUtil.class);
            MockedStatic<NetUtils> netUtils = mockStatic(NetUtils.class)){
            netUtils.when(NetUtils::getIpAddress).thenReturn("127.0.0.1");
            // response has error
            CompletableFuture<HttpClientResponse> errorFuture = new CompletableFuture<>();
            errorFuture.completeExceptionally(new RuntimeException("mock error"));
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), anyMap())).thenReturn(errorFuture);
            ConfigService.INSTANCE.reportStatus();
            assertFalse(ConfigService.INSTANCE.reloadConfig());

            // response header is empty
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), anyMap())).thenReturn(
                CompletableFuture.completedFuture(HttpClientResponse.emptyResponse()));
            ConfigService.INSTANCE.reportStatus();
            assertFalse(ConfigService.INSTANCE.reloadConfig());

            // lastModified is null
            Map<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put("Last-Modified2", "Thu, 01 Jan 1970 00:00:00 GMT");
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), anyMap())).thenReturn(
                CompletableFuture.completedFuture(new HttpClientResponse(200, responseHeaders, null)));
            ConfigService.INSTANCE.reportStatus();
            assertFalse(ConfigService.INSTANCE.reloadConfig());

            // if-Modified-Since == lastModified, prevLastModified is null
            responseHeaders.put("Last-Modified", "Thu, 01 Jan 1970 00:00:00 GMT");
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), anyMap())).thenReturn(
                CompletableFuture.completedFuture(new HttpClientResponse(200, responseHeaders, null)));
            ConfigService.INSTANCE.reportStatus();
            assertFalse(ConfigService.INSTANCE.reloadConfig());

            // if-Modified-Since != lastModified
            responseHeaders.put("Last-Modified", "Thu, 02 Jan 1970 00:00:00 GMT");
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), anyMap())).thenReturn(
                CompletableFuture.completedFuture(new HttpClientResponse(200, responseHeaders, null)));
            ConfigService.INSTANCE.reportStatus();
            assertTrue(ConfigService.INSTANCE.reloadConfig());

            // if-Modified-Since == lastModified
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), anyMap())).thenReturn(
                CompletableFuture.completedFuture(new HttpClientResponse(200, responseHeaders, null)));
            ConfigService.INSTANCE.reportStatus();
            assertFalse(ConfigService.INSTANCE.reloadConfig());
        }
    }

    @Test
    void shutdown() {
        try (MockedStatic<AsyncHttpClientUtil> ahc = mockStatic(AsyncHttpClientUtil.class);
            MockedStatic<NetUtils> netUtils = mockStatic(NetUtils.class)){
            netUtils.when(NetUtils::getIpAddress).thenReturn("127.0.0.1");
            Map<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put("Last-Modified2", "Thu, 01 Jan 1970 00:00:00 GMT");
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), anyMap())).thenReturn(
                CompletableFuture.completedFuture(new HttpClientResponse(200, responseHeaders, null)));

            ConfigService.INSTANCE.shutdown();

            AgentStatusEnum actualResult = ConfigService.INSTANCE.getAgentStatus();
            assertEquals(AgentStatusEnum.SHUTDOWN, actualResult);

            ConfigService.INSTANCE.shutdown();
        }
    }

    @Test
    void buildConfigQueryRequest() {
        ConfigQueryRequest request = ConfigService.INSTANCE.buildConfigQueryRequest();
        assertEquals("fat", request.getSystemProperties().get("arex.tags.env"));
        assertEquals(1, request.getSystemProperties().size());
    }
}
