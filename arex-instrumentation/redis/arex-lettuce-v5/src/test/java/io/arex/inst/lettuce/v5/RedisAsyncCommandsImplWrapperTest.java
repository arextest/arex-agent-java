package io.arex.inst.lettuce.v5;

import io.arex.inst.redis.common.RedisConnectionManager;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisExtractor;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.protocol.AsyncCommand;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.ProtocolKeyword;
import io.lettuce.core.protocol.RedisCommand;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class RedisAsyncCommandsImplWrapperTest {
    static RedisAsyncCommandsImplWrapper target;
    static Command cmd;
    static StatefulRedisConnection connection = Mockito.mock(StatefulRedisConnection.class);
    @BeforeAll
    static void setUp() {
        cmd = Mockito.mock(Command.class);
        try (MockedConstruction<RedisCommandBuilderImpl> mocked = Mockito.mockConstruction(RedisCommandBuilderImpl.class, (mock, context) -> {
            Mockito.when(mock.hget(any(), any())).thenReturn(cmd);
        })) {}
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success("mock"));
        })) {}
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RedisConnectionManager.class);
        target = new RedisAsyncCommandsImplWrapper(connection, Mockito.mock(RedisCodec.class));
    }

    @AfterAll
    static void tearDown() {
        target = null;
        cmd = null;
        connection = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("dispatchCase")
    void dispatch(Runnable mocker, Predicate<RedisFuture<?>> predicate, MockResult mockResult) {
        mocker.run();
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class, (extractor, context) -> {
            System.out.println("mock RedisExtractor");
            Mockito.when(extractor.replay()).thenReturn(mockResult);
            Mockito.doNothing().when(extractor).record(any());
        })) {
            RedisFuture<?> result = target.hget("key", "field");
            assertTrue(predicate.test(result));
        }
    }

    static Stream<Arguments> dispatchCase() {
        Runnable mocker1 = () -> {
            Mockito.when(RedisConnectionManager.getRedisUri(anyInt())).thenReturn("");
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            Mockito.when(cmd.getType()).thenReturn(Mockito.mock(ProtocolKeyword.class));
        };
        Runnable mocker2 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            AsyncCommand command = new AsyncCommand(cmd);
            command.completeExceptionally(new NullPointerException());
            Mockito.when(connection.dispatch(any(RedisCommand.class))).thenReturn(command);
        };
        Runnable mocker3 = () -> {
            AsyncCommand command = new AsyncCommand(cmd);
            command.complete("mock");
            Mockito.when(connection.dispatch(any(RedisCommand.class))).thenReturn(command);
        };
        Runnable mocker4 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
        };
        Predicate<RedisFuture<?>> predicate1 = Objects::nonNull;
        return Stream.of(
            arguments(mocker1, predicate1, MockResult.success(null)),
            arguments(mocker1, predicate1, MockResult.success(new NullPointerException())),
            arguments(mocker2, predicate1, null),
            arguments(mocker3, predicate1, null),
            arguments(mocker4, predicate1, null)
        );
    }
}
