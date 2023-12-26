package io.arex.inst.lettuce.v6.cluster.inst;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.inst.lettuce.v6.cluster.RedisClusterReactiveCommandsImplWrapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

public class StatefulRedisClusterConnectionImplInstrumentationTest {

    static StatefulRedisClusterConnectionImplInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new StatefulRedisClusterConnectionImplInstrumentation();
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @Test
    void methodAdvices() {
        assertNotNull(target.methodAdvices());
    }

    @Test
    void onEnter() {
        assertTrue(StatefulRedisClusterConnectionImplInstrumentation.NewRedisAsyncCommandsImplAdvice.onEnter());
    }

    @Test
    void onExit() throws Exception {
        assertDoesNotThrow(() ->
            StatefulRedisClusterConnectionImplInstrumentation
                .NewRedisAsyncCommandsImplAdvice.onExit(null, null, null));
    }

    @Test
    void reactiveOnEnter() {
        assertTrue(StatefulRedisClusterConnectionImplInstrumentation.NewRedisReactiveCommandsImplAdvice.onEnter());
    }

    @Test
    void reactiveOnExit() throws Exception {
        try (MockedConstruction<RedisClusterReactiveCommandsImplWrapper> mocked = Mockito.mockConstruction(RedisClusterReactiveCommandsImplWrapper.class,
            (mock, context) -> {
            })) {
            assertDoesNotThrow(() -> StatefulRedisClusterConnectionImplInstrumentation
                .NewRedisReactiveCommandsImplAdvice.onExit(null, null, null));
        }
    }
}
