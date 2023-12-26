package io.arex.inst.lettuce.v5.standalone.inst;

import io.arex.inst.lettuce.v5.standalone.RedisReactiveCommandsImplWrapper;
import io.arex.inst.redis.common.RedisConnectionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class StatefulRedisConnectionImplInstrumentationTest {
    static StatefulRedisConnectionImplInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new StatefulRedisConnectionImplInstrumentation();
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
        assertTrue(StatefulRedisConnectionImplInstrumentation.NewRedisAsyncCommandsImplAdvice.onEnter());
    }

    @Test
    void onExit() throws Exception {
        assertDoesNotThrow(() ->
            StatefulRedisConnectionImplInstrumentation
                .NewRedisAsyncCommandsImplAdvice.onExit(null, null, null));
    }

    @Test
    void reactiveOnEnter() {
        assertTrue(StatefulRedisConnectionImplInstrumentation.NewRedisReactiveCommandsImplAdvice.onEnter());
    }

    @Test
    void reactiveOnExit() throws Exception {
        try (MockedConstruction<RedisReactiveCommandsImplWrapper> mocked = Mockito.mockConstruction(RedisReactiveCommandsImplWrapper.class,
                (mock, context) -> {
        })) {
            assertDoesNotThrow(() -> StatefulRedisConnectionImplInstrumentation
                    .NewRedisReactiveCommandsImplAdvice.onExit(null, null, null));
        }
    }
}