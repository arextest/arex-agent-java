package io.arex.inst.redis.common.lettuce.wrapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.common.util.FluxReplayUtil.FluxElementResult;
import io.arex.inst.common.util.FluxReplayUtil.FluxResult;
import io.arex.inst.redis.common.RedisConnectionManager;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.redis.common.lettuce.RedisCommandBuilderImpl;
import io.arex.inst.runtime.context.ContextManager;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisReactiveCommandsImpl;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.ProtocolKeyword;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.tracing.Tracing;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.Spy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RedisReactiveCommandWrapperTest {

    @Spy
    @InjectMocks
    static RedisReactiveCommandWrapper target;
    static Command cmd;
    static StatefulRedisConnection connection;
    static RedisReactiveCommandsImpl reactiveCommands;

    @BeforeAll
    static void setUp() {
        connection = Mockito.mock(StatefulRedisConnection.class);
        cmd = Mockito.mock(Command.class);
        try (MockedConstruction<RedisCommandBuilderImpl> mocked = Mockito.mockConstruction(RedisCommandBuilderImpl.class, (mock, context) -> {
            Mockito.when(mock.hget(any(), any())).thenReturn(cmd);
        })) {}
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RedisConnectionManager.class);
        ClientResources resources = Mockito.mock(ClientResources.class);
        Tracing trace = Mockito.mock(Tracing.class);
        Mockito.when(resources.tracing()).thenReturn(trace);
        Mockito.when(connection.getResources()).thenReturn(resources);
        ClientOptions options = Mockito.mock(ClientOptions.class);
        Mockito.when(connection.getOptions()).thenReturn(options);
        target = new RedisReactiveCommandWrapper(Mockito.mock(RedisCodec.class));
        reactiveCommands = Mockito.mock(RedisReactiveCommandsImpl.class);
        Mockito.when(RedisConnectionManager.getRedisUri(anyInt())).thenReturn("");
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(cmd.getType()).thenReturn(Mockito.mock(ProtocolKeyword.class));
    }

    @ParameterizedTest
    @MethodSource("monoDispatchCase")
    void createMono(Runnable mocker, Predicate<Mono<?>> predicate, MockResult mockResult) {
        mocker.run();
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class,
            (extractor, context) -> {
                Mockito.when(extractor.replay()).thenReturn(mockResult);
            })) {
            Mono<?> result = target.createMono(reactiveCommands,"127.0.0.1:6379",() -> cmd, "key");
            assertTrue(predicate.test(result));
        }
    }

    @ParameterizedTest
    @MethodSource("fluxDispatchCase")
    void createDissolvingFlux(Runnable mocker, Predicate<Flux<?>> predicate, MockResult mockResult) {
        mocker.run();
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class,
            (extractor, context) -> {
                Mockito.when(extractor.replay()).thenReturn(mockResult);
            })) {
            Flux<?> result = target.createDissolvingFlux(reactiveCommands,"127.0.0.1:6379",() -> cmd, "key");
            assertTrue(predicate.test(result));
        }
    }

    static Stream<Arguments> monoDispatchCase() {
        Runnable replayMocker = () -> {
            Mockito.when(RedisConnectionManager.getRedisUri(anyInt())).thenReturn("");
            Mockito.when(ContextManager.needReplay()).thenReturn(true);

        };

        Runnable recordWithResultMocker = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.when(reactiveCommands.createMono(any(Supplier.class))).thenReturn(Mono.just("mock"));
            Mockito.when(reactiveCommands.createDissolvingFlux(any(Supplier.class))).thenReturn(Flux.just("mock"));
        };

        Runnable recordWithExceptionMocker = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(reactiveCommands.createMono(any(Supplier.class))).thenReturn(Mono.error(new RuntimeException()));
            Mockito.when(reactiveCommands.createDissolvingFlux(any(Supplier.class))).thenReturn(Flux.error(new RuntimeException()));
        };

        return Stream.of(
            arguments(replayMocker, (Predicate<Mono<?>>) mono -> "mock".equals(mono.block()), MockResult.success("mock")),
            arguments(replayMocker, (Predicate<Mono<?>>) mono -> assertThrows(RuntimeException.class, mono::block) != null, MockResult.success(new RuntimeException())),
            arguments(recordWithResultMocker, (Predicate<Mono<?>>) mono -> "mock".equals(mono.block()), null),
            arguments(recordWithExceptionMocker, (Predicate<Mono<?>>) mono -> assertThrows(RuntimeException.class, mono::block) != null, null));
    }

    static Stream<Arguments> fluxDispatchCase() {
        Runnable replayMocker = () -> {
            Mockito.when(RedisConnectionManager.getRedisUri(anyInt())).thenReturn("");
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };

        Runnable recordWithResultMocker = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.when(reactiveCommands.createDissolvingFlux(any(Supplier.class))).thenReturn(Flux.just("mock"));
        };

        Runnable recordWithExceptionMocker = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(reactiveCommands.createDissolvingFlux(any(Supplier.class))).thenReturn(Flux.error(new RuntimeException()));
        };
        List<FluxElementResult> fluxElementResults = new ArrayList<>();
        FluxResult fluxResult = new FluxResult("java.lang.String", fluxElementResults);

        return Stream.of(
            arguments(replayMocker, (Predicate<Flux<?>>) flux -> !("mock".equals(flux.blockFirst())), MockResult.success(fluxResult)),
            arguments(replayMocker, (Predicate<Flux<?>>) flux -> assertThrows(RuntimeException.class, flux::blockFirst) != null, MockResult.success(new RuntimeException())),
            arguments(recordWithResultMocker, (Predicate<Flux<?>>) flux -> "mock".equals(flux.blockFirst()), null),
            arguments(recordWithExceptionMocker, (Predicate<Flux<?>>) flux -> assertThrows(RuntimeException.class, flux::blockFirst) != null, null)
        );
    }
}
