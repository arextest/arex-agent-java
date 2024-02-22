package io.arex.agent.instrumentation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import io.arex.agent.bootstrap.cache.AdviceInjectorCache;
import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.AdviceClassesCollector;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.model.ConfigQueryResponse;
import io.arex.foundation.model.ConfigQueryResponse.ResponseBody;
import io.arex.foundation.model.ConfigQueryResponse.ServiceCollectConfig;
import io.arex.foundation.model.HttpClientResponse;
import io.arex.foundation.serializer.jackson.JacksonSerializer;
import io.arex.foundation.util.NetUtils;
import io.arex.foundation.util.httpclient.AsyncHttpClientUtil;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class BaseAgentInstallerTest {
    static BaseAgentInstaller installer = null;
    @BeforeAll
    static void beforeAll() {
        mockStatic(AdviceInjectorCache.class);

        Instrumentation inst = Mockito.mock(Instrumentation.class);
        File file = Mockito.mock(File.class);
        installer = new BaseAgentInstaller(inst, file, null) {
            @Override
            protected void transform() {
            }

            @Override
            protected void retransform() {

            }
        };
    }

    @AfterAll
    static void afterAll() {
        installer = null;
        Mockito.clearAllCaches();
    }

    @Test
    void install() throws Throwable {
        Mockito.when(AdviceInjectorCache.contains(any())).thenReturn(true);
        try (MockedStatic<AsyncHttpClientUtil> ahc = mockStatic(AsyncHttpClientUtil.class);
            MockedConstruction<AdviceClassesCollector> ignored = Mockito.mockConstruction(
            AdviceClassesCollector.class, (mock, context) -> {
                Mockito.verify(mock, Mockito.times(1)).addClassToLoaderSearch(JacksonSerializer.class);
            })) {

            // allow start agent = false
            ConfigQueryResponse configQueryResponse = new ConfigQueryResponse();
            ServiceCollectConfig serviceCollectConfig = new ServiceCollectConfig();
            serviceCollectConfig.setAllowDayOfWeeks(127);
            serviceCollectConfig.setAllowTimeOfDayFrom("00:00");
            serviceCollectConfig.setAllowTimeOfDayTo("23:59");
            serviceCollectConfig.setSampleRate(1);
            ResponseBody responseBody = new ResponseBody();
            responseBody.setAgentEnabled(false);
            responseBody.setServiceCollectConfiguration(serviceCollectConfig);
            configQueryResponse.setBody(responseBody);
            CompletableFuture<HttpClientResponse> response = CompletableFuture.completedFuture(new HttpClientResponse(200, null, JacksonSerializer.INSTANCE.serialize(configQueryResponse)));
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), any())).thenReturn(response);

            ConfigManager.INSTANCE.setEnableDebug("false");
            assertDoesNotThrow(installer::install);

            // first transform class, allow start agent = true
            responseBody.setAgentEnabled(true);
            configQueryResponse.setBody(responseBody);
            response = CompletableFuture.completedFuture(new HttpClientResponse(200, null, JacksonSerializer.INSTANCE.serialize(configQueryResponse)));
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), eq(null))).thenReturn(response);
            assertDoesNotThrow(installer::install);

            // retransform
            assertDoesNotThrow(installer::install);
        }
    }

    @Test
    void allowStartAgent() {
        ConfigManager.INSTANCE.setStorageServiceMode(ConfigConstants.STORAGE_MODE);
        assertTrue(installer.allowStartAgent());
        ConfigManager.INSTANCE.setStorageServiceMode("not " + ConfigConstants.STORAGE_MODE);
        ConfigManager.INSTANCE.setAgentEnabled(false);
        assertFalse(installer.allowStartAgent());
    }

    @Test
    void getInvalidReason() {
        ConfigManager.INSTANCE.setAgentEnabled(false);
        ConfigManager.INSTANCE.setMessage("127.0.0.1 is not active ip");
        assertEquals("127.0.0.1 is not active ip", installer.getInvalidReason());
        // checkTargetAddress = true
        ConfigManager.INSTANCE.setAgentEnabled(true);
        assertEquals("invalid config", installer.getInvalidReason());
    }
}
