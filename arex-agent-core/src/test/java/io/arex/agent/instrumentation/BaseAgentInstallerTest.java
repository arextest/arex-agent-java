package io.arex.agent.instrumentation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import io.arex.agent.bootstrap.cache.AdviceInjectorCache;
import io.arex.agent.bootstrap.util.AdviceClassesCollector;
import io.arex.foundation.serializer.JacksonSerializer;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class BaseAgentInstallerTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(AdviceInjectorCache.class);
    }

    @Test
    void install() {
        Mockito.when(AdviceInjectorCache.contains(any())).thenReturn(true);
        try (MockedConstruction<AdviceClassesCollector> collectorMockedConstruction = Mockito.mockConstruction(AdviceClassesCollector.class,
                (mock, context) -> {
                    Mockito.verify(mock, Mockito.times(1)).addClassToLoaderSearch(JacksonSerializer.class);
                })) {
            new BaseAgentInstaller(ByteBuddyAgent.install(), null, null) {
                @Override
                protected ResettableClassFileTransformer transform() {
                    return null;
                }
            }.install();
        }
    }
}