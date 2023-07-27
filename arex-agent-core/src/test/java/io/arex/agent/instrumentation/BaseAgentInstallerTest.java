package io.arex.agent.instrumentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import io.arex.agent.bootstrap.cache.AdviceInjectorCache;
import io.arex.agent.bootstrap.util.AdviceClassesCollector;
import io.arex.foundation.model.HttpClientResponse;
import io.arex.foundation.serializer.JacksonSerializer;
import io.arex.foundation.util.NetUtils;
import io.arex.foundation.util.httpclient.AsyncHttpClientUtil;
import java.util.concurrent.CompletableFuture;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class BaseAgentInstallerTest {

    @BeforeAll
    static void beforeAll() {
        mockStatic(AdviceInjectorCache.class);
    }

    @AfterAll
    static void afterAll() {
        Mockito.clearAllCaches();
    }

    @Test
    void install() {
        Mockito.when(AdviceInjectorCache.contains(any())).thenReturn(true);
        try (MockedStatic<AsyncHttpClientUtil> ahc = mockStatic(AsyncHttpClientUtil.class);
            MockedStatic<NetUtils> netUtils = mockStatic(NetUtils.class);
            MockedConstruction<AdviceClassesCollector> ignored = Mockito.mockConstruction(
            AdviceClassesCollector.class, (mock, context) -> {
                Mockito.verify(mock, Mockito.times(1)).addClassToLoaderSearch(JacksonSerializer.class);
            })) {

            netUtils.when(NetUtils::getIpAddress).thenReturn("127.0.0.1");
            ahc.when(() -> AsyncHttpClientUtil.postAsyncWithJson(anyString(), anyString(), any())).thenReturn(
                CompletableFuture.completedFuture(HttpClientResponse.emptyResponse()));

            BaseAgentInstaller installer = new BaseAgentInstaller(ByteBuddyAgent.install(), null, null) {
                @Override
                protected ResettableClassFileTransformer transform() {
                    return null;
                }
            };

            installer.install();
        }
    }
}
