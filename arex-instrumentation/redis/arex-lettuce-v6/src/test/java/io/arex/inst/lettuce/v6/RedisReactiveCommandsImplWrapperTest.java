package io.arex.inst.lettuce.v6;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.model.MockResult;
import io.arex.inst.redis.common.RedisExtractor;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.ProtocolKeyword;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.tracing.Tracing;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class RedisReactiveCommandsImplWrapperTest {

    static RedisReactiveCommandsImplWrapper target;
    static Command cmd;
    static StatefulRedisConnection connection;

    @BeforeAll
    static void setUp() {
        connection = Mockito.mock(StatefulRedisConnection.class);
        cmd = Mockito.mock(Command.class);
        try (MockedConstruction<RedisCommandBuilderImpl> mocked = Mockito.mockConstruction(RedisCommandBuilderImpl.class, (mock, context) -> {
            Mockito.when(mock.hget(any(), any())).thenReturn(cmd);
        })) {}
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.of("mock"));
        })) {}
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(LettuceHelper.class);
        ClientResources resources = Mockito.mock(ClientResources.class);
        Tracing trace = Mockito.mock(Tracing.class);
        Mockito.when(resources.tracing()).thenReturn(trace);
        Mockito.when(connection.getResources()).thenReturn(resources);
        ClientOptions options = Mockito.mock(ClientOptions.class);
        Mockito.when(connection.getOptions()).thenReturn(options);
        target = new RedisReactiveCommandsImplWrapper(connection, Mockito.mock(RedisCodec.class));
        Mockito.when(LettuceHelper.getRedisUri(anyInt())).thenReturn("");
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(cmd.getType()).thenReturn(Mockito.mock(ProtocolKeyword.class));
    }

    @AfterAll
    static void tearDown() {
        target = null;
        cmd = null;
        connection = null;
        Mockito.clearAllCaches();
    }

    @Test
    void createMono() {
        assertNotNull(target.createMono(() -> cmd,  "key", "field"));
    }

    @Test
    void createDissolvingFlux() {
        assertNotNull(target.createDissolvingFlux(() -> cmd,  "key", "field"));
    }
}