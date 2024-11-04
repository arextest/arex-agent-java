package io.arex.inst.spring.data.redis;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RedisTemplateProviderTest {

    private static final String KEY = "key";
    private static final String RESTULT = "result";
    private static final String METHOD_NAME = "get";
    private static final String REDIS_URI = "127.0.0.1";

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
        Mockito.mockStatic(Config.class);
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.getRecordVersion()).thenReturn("0.4.2");
        Mockito.when(Config.get()).thenReturn(config);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("mockStream")
    void methodOnEnter(Runnable mocker) {
        mocker.run();
        RedisTemplateProvider.methodOnEnter(REDIS_URI, METHOD_NAME, KEY);
    }

    @ParameterizedTest
    @MethodSource("mockStream")
    void methodExit(Runnable mocker) {
        mocker.run();
        // no throwable
        RedisTemplateProvider.methodOnExit(REDIS_URI, METHOD_NAME, KEY, RESTULT, null);
        // no result
        RedisTemplateProvider.methodOnExit(REDIS_URI, METHOD_NAME, KEY, null, new Throwable());
    }

    private static Stream<Arguments> mockStream() {

        // not needRecord & not needReplay
        Runnable mocker = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
        };

        // needReplay
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            Mockito.when(RepeatedCollectManager.exitAndValidate(Mockito.anyString())).thenReturn(false);
        };

        // needRecord & not repeatEnter
        Runnable mocker2 = () -> {
            Mockito.mock(ContextManager.class);
            Mockito.when(RepeatedCollectManager.exitAndValidate(Mockito.anyString())).thenReturn(true);
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };

        // needRecord & repeatEnter
        Runnable mocker3 = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate(Mockito.anyString())).thenReturn(false);
        };

        return Stream.of(arguments(mocker), arguments(mocker1), arguments(mocker2), arguments(mocker3));
    }
}
