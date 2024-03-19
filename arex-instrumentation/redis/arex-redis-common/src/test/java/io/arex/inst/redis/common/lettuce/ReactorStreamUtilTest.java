package io.arex.inst.redis.common.lettuce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.reactorcore.common.FluxReplayUtil.FluxElementResult;
import io.arex.inst.reactorcore.common.FluxReplayUtil.FluxResult;
import io.arex.inst.redis.common.RedisConnectionManager;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.redis.common.RedisExtractor.RedisMultiKey;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactorStreamUtilTest {

    static RedisExtractor extractor;

    @BeforeAll
    static void setUp() {
        extractor = new RedisExtractor("", "", "", "");
    }

    @AfterAll
    static void tearDown() {
        extractor = null;
        Mockito.clearAllCaches();
    }

    @Test
    void monoRecord() {
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class,
            (extractor, context) -> {
                System.out.println("mock ReactorStreamUtil monoRecord");
                Mockito.doNothing().when(extractor).record(any());
            })) {
            Mono<String> mono = Mono.just("test");
            ReactorStreamUtil.monoRecord("127.1.1.0", mono, "test", "key", "field");
            String result = mono.block();
            assertNotNull(result);
        }
    }

    @Test
    void fluxRecord() {
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class,
            (extractor, context) -> {
                System.out.println("mock ReactorStreamUtil monoRecord");
                Mockito.doNothing().when(extractor).record(any());
            })) {
            Flux<String> flux = Flux.just("test");
            ReactorStreamUtil.fluxRecord("127.1.1.0", flux, "test", "key", "field");
            String result = flux.blockFirst();
            assertNotNull(result);
        }
    }

    @Test
    void setExtractor(){
        RedisMultiKey multiKey = new RedisMultiKey( );
        multiKey.setKey("key");
        multiKey.setField("field");
        assertEquals("key",multiKey.getKey());
        assertEquals("field",multiKey.getField());
    }

    @ParameterizedTest
    @MethodSource("monoReplayCase")
    void monoReplay(Predicate<Mono<?>> predicate, MockResult mockResult) {
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class,
            (extractor, context) -> {
                Mockito.when(extractor.replay()).thenReturn(mockResult);
            })) {
            Mono<?> result = ReactorStreamUtil.monoReplay(RedisConnectionManager.getRedisUri(0), "test",
                "key", "field");
            assertTrue(predicate.test(result));
        }
    }

    @ParameterizedTest
    @MethodSource("fluxReplayCase")
    void fluxReplay(Predicate<Flux<?>> predicate, MockResult mockResult) {
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class,
            (extractor, context) -> {
                Mockito.when(extractor.replay()).thenReturn(mockResult);
            })) {
            Flux<?> result = ReactorStreamUtil.fluxReplay(RedisConnectionManager.getRedisUri(0), "test",
                "key", "field");
            assertTrue(predicate.test(result));
        }
    }

    static Stream<Arguments> monoReplayCase() {
        return Stream.of(
            arguments((Predicate<Mono<?>>) mono -> "mock".equals(mono.block()), MockResult.success(false, "mock")),
            arguments((Predicate<Mono<?>>) mono -> assertThrows(RuntimeException.class, mono::block) != null,
                MockResult.success(false, new RuntimeException())),
            arguments((Predicate<Mono<?>>) mono -> mono.block() == null, MockResult.success(true, "mock")),
            arguments((Predicate<Mono<?>>) mono -> mono.block() == null,
                MockResult.success(true, new RuntimeException())));
    }

    static Stream<Arguments> fluxReplayCase() {

        List<FluxElementResult> fluxElementResults = new ArrayList<>();
        FluxResult fluxResult = new FluxResult("java.lang.String", fluxElementResults);
        return Stream.of(
            arguments((Predicate<Flux<?>>) flux -> !("mock".equals(flux.blockFirst())), MockResult.success(false, fluxResult)),
            arguments((Predicate<Flux<?>>) flux -> assertThrows(RuntimeException.class, flux::blockFirst) != null,
                MockResult.success(false, new RuntimeException())),
            arguments((Predicate<Flux<?>>) flux -> flux.blockFirst() == null, MockResult.success(true, "mock")),
            arguments((Predicate<Flux<?>>) flux -> flux.blockFirst() == null,
                MockResult.success(true, new RuntimeException())));
    }


}
