package io.arex.foundation.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.serializer.JacksonSerializer;
import io.arex.foundation.services.ConfigService.ConfigQueryRequest;
import io.arex.foundation.services.ConfigService.ConfigQueryResponse;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.NetUtils;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ConfigServiceTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void loadAgentConfig() {
        ConfigService.INSTANCE.loadAgentConfig("arex.service.name=unit-test-service;arex.enable.debug=true");

        try (MockedStatic<Serializer> serializer = mockStatic(Serializer.class);
            MockedStatic<AsyncHttpClientUtil> ahc = mockStatic(AsyncHttpClientUtil.class);){
            serializer.when(() -> Serializer.serialize(any())).thenReturn("{\"appId\":\"unit-test-service\",\"host\":\"127.0.0.1\"}");

            // invalid response
            ahc.when(() -> AsyncHttpClientUtil.post(anyString(), anyString())).thenReturn("{}");
            ConfigService.INSTANCE.loadAgentConfig(null);

            // valid response
            String responseJson = "{\"responseStatusType\":{\"responseCode\":0,\"responseDesc\":\"success\",\"timestamp\":1675072242788},\"body\":{\"serviceCollectConfiguration\":{\"status\":null,\"modifiedTime\":null,\"appId\":\"community.test.mark20221214\",\"sampleRate\":1,\"allowDayOfWeeks\":127,\"timeMock\":true,\"allowTimeOfDayFrom\":\"00:01\",\"allowTimeOfDayTo\":\"23:59\",\"excludeServiceOperationSet\":null},\"dynamicClassConfigurationList\":null,\"status\":3}}";
            ahc.when(() -> AsyncHttpClientUtil.post(anyString(), anyString())).thenReturn(responseJson);
            ConfigQueryResponse response = JacksonSerializer.INSTANCE.deserialize(responseJson, ConfigQueryResponse.class);
            serializer.when(() -> Serializer.deserialize(responseJson, ConfigQueryResponse.class)).thenReturn(response);

            ConfigService.INSTANCE.loadAgentConfig(null);
        }
    }
}