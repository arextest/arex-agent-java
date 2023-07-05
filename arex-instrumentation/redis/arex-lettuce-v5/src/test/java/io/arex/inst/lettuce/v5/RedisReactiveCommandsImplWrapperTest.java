package io.arex.inst.lettuce.v5;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisConnectionManager;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.ProtocolKeyword;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.tracing.Tracing;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class RedisReactiveCommandsImplWrapperTest {

    @Spy
    @InjectMocks
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
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RedisConnectionManager.class);
        ClientResources resources = Mockito.mock(ClientResources.class);
        Tracing trace = Mockito.mock(Tracing.class);
        Mockito.when(resources.tracing()).thenReturn(trace);
        Mockito.when(connection.getResources()).thenReturn(resources);
        ClientOptions options = Mockito.mock(ClientOptions.class);
        Mockito.when(connection.getOptions()).thenReturn(options);
        target = new RedisReactiveCommandsImplWrapper(connection, Mockito.mock(RedisCodec.class));
        Mockito.when(RedisConnectionManager.getRedisUri(anyInt())).thenReturn("");
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

    @ParameterizedTest
    @MethodSource("monoDispatchCase")
    void createMono(Runnable mocker, Predicate<Mono<?>> predicate, MockResult mockResult) {
        mocker.run();
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class, (extractor, context) -> {
            System.out.println("mock RedisExtractor");
            Mockito.when(extractor.replay()).thenReturn(mockResult);
        })) {
            Mono<?> result = target.createMono(() -> cmd,  "key", "field");
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
            Mockito.doReturn(Mono.just("mock")).when(target).createMono(any(Supplier.class));
            Mockito.doReturn(Flux.just("mock")).when(target).createDissolvingFlux(any());
        };

        Runnable recordWithExceptionMocker = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.doReturn(Mono.error(new RuntimeException())).when(target).createMono(any(Supplier.class));
            Mockito.doReturn(Flux.error(new RuntimeException())).when(target).createDissolvingFlux(any());
        };

        return Stream.of(
            arguments(replayMocker, (Predicate<Mono<?>>) mono -> "mock".equals(mono.block()), MockResult.success("mock")),
            arguments(replayMocker, (Predicate<Mono<?>>) mono -> assertThrows(RuntimeException.class, mono::block) != null, MockResult.success(new RuntimeException())),
            arguments(recordWithResultMocker, (Predicate<Mono<?>>) mono -> "mock".equals(mono.block()), null),
            arguments(recordWithExceptionMocker, (Predicate<Mono<?>>) mono -> assertThrows(RuntimeException.class, mono::block) != null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("fluxDispatchCase")
    void createDissolvingFlux(Runnable mocker, Predicate<Flux<?>> predicate, MockResult mockResult) {
        mocker.run();
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class, (extractor, context) -> {
            System.out.println("mock RedisExtractor");
            Mockito.when(extractor.replay()).thenReturn(mockResult);
        })) {
            Flux<?> result = target.createDissolvingFlux(() -> cmd,  "key", "field");
            assertTrue(predicate.test(result));
        }
    }

    static Stream<Arguments> fluxDispatchCase() {
        Runnable replayMocker = () -> {
            Mockito.when(RedisConnectionManager.getRedisUri(anyInt())).thenReturn("");
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };

        Runnable recordWithResultMocker = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.doReturn(Flux.just("mock")).when(target).createDissolvingFlux(any());
        };

        Runnable recordWithExceptionMocker = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.doReturn(Flux.error(new RuntimeException())).when(target).createDissolvingFlux(any());
        };

        return Stream.of(
            arguments(replayMocker, (Predicate<Flux<?>>) flux -> "mock".equals(flux.blockFirst()), MockResult.success("mock")),
            arguments(replayMocker, (Predicate<Flux<?>>) flux -> assertThrows(RuntimeException.class, flux::blockFirst) != null, MockResult.success(new RuntimeException())),
            arguments(recordWithResultMocker, (Predicate<Flux<?>>) flux -> "mock".equals(flux.blockFirst()), null),
            arguments(recordWithExceptionMocker, (Predicate<Flux<?>>) flux -> assertThrows(RuntimeException.class, flux::blockFirst) != null, null)
        );
    }
}
