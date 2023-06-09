package io.arex.foundation.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.config.ConfigQueryResponse;
import io.arex.foundation.config.ConfigQueryResponse.ResponseBody;
import io.arex.foundation.config.ConfigQueryResponse.ServiceCollectConfig;
import io.arex.foundation.serializer.JacksonSerializer;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.NetUtils;
import java.time.DayOfWeek;
import java.util.EnumSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ConfigServiceTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void loadAgentConfig() {
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
            // responseJson = {}
            ahc.when(() -> AsyncHttpClientUtil.post(anyString(), anyString())).thenReturn("{}");
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertEquals(0, ConfigManager.INSTANCE.getRecordRate());
            assertEquals(EnumSet.noneOf(DayOfWeek.class), ConfigManager.INSTANCE.getAllowDayOfWeeks());

            // response body serviceCollectConfiguration is null
            netUtils.when(NetUtils::getIpAddress).thenReturn("127.0.0.3");
            ahc.when(() -> AsyncHttpClientUtil.post(anyString(), anyString())).thenReturn("{\"body\":{}}");
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertEquals(0, ConfigManager.INSTANCE.getRecordRate());
            assertEquals(EnumSet.noneOf(DayOfWeek.class), ConfigManager.INSTANCE.getAllowDayOfWeeks());

            // valid response -> AgentStatus.WORKING
            netUtils.when(NetUtils::getIpAddress).thenReturn("127.0.0.1");
            ConfigQueryResponse configQueryResponse = new ConfigQueryResponse();

            ServiceCollectConfig serviceCollectConfig = new ServiceCollectConfig();
            serviceCollectConfig.setAllowDayOfWeeks(127);
            serviceCollectConfig.setAllowTimeOfDayFrom("00:00");
            serviceCollectConfig.setAllowTimeOfDayTo("23:59");
            serviceCollectConfig.setSampleRate(1);

            ResponseBody responseBody = new ResponseBody();
            responseBody.setTargetAddress("127.0.0.1");
            responseBody.setServiceCollectConfiguration(serviceCollectConfig);
            configQueryResponse.setBody(responseBody);
            ahc.when(() -> AsyncHttpClientUtil.post(anyString(), anyString())).thenReturn(JacksonSerializer.INSTANCE.serialize(configQueryResponse));
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertTrue(ConfigManager.INSTANCE.valid() && ConfigManager.INSTANCE.inWorkingTime() && ConfigManager.INSTANCE.getRecordRate() > 0);

            ConfigManager.FIRST_TRANSFORM.compareAndSet(false, true);
            // valid response request agentStatus=WORKING
            serviceCollectConfig.setAllowDayOfWeeks(0);
            ahc.when(() -> AsyncHttpClientUtil.post(anyString(), anyString())).thenReturn(JacksonSerializer.INSTANCE.serialize(configQueryResponse));
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertFalse(ConfigManager.INSTANCE.inWorkingTime());

            // valid response request agentStatus=SLEEPING
            serviceCollectConfig.setAllowDayOfWeeks(127);
            ahc.when(() -> AsyncHttpClientUtil.post(anyString(), anyString())).thenReturn(JacksonSerializer.INSTANCE.serialize(configQueryResponse));
            assertEquals(DELAY_MINUTES, ConfigService.INSTANCE.loadAgentConfig(null));
            assertTrue(ConfigManager.INSTANCE.valid() && ConfigManager.INSTANCE.inWorkingTime() && ConfigManager.INSTANCE.getRecordRate() > 0);
        }
    }
}
