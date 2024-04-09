package io.arex.inst.redisson.v3;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisExtractor;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RFuture;
import org.redisson.misc.CompletableFutureWrapper;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class RedissonWrapperCommonTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("delegateCallCase")
    void delegateCall(Runnable mocker, Supplier<RFuture<String>> futureSupplier, Predicate<RFuture> predicate) {
        try(MockedConstruction<RedisExtractor> extractor =   Mockito.mockConstruction(RedisExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success(true,"mock"));
        })) {
            mocker.run();
            RFuture result = RedissonWrapperCommon.delegateCall("", "", "", futureSupplier);
            assertTrue(predicate.test(result));
        }

        try(MockedConstruction<RedisExtractor> extractor =   Mockito.mockConstruction(RedisExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success(false,"mock"));
        })) {
            mocker.run();
            RFuture result = RedissonWrapperCommon.delegateCall("", "", "", futureSupplier);
            assertTrue(predicate.test(result));
        }

        try(MockedConstruction<RedisExtractor> extractor =   Mockito.mockConstruction(RedisExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success(new Throwable()));
        })) {
            mocker.run();
            RFuture result = RedissonWrapperCommon.delegateCall("", "", "", futureSupplier);
            assertTrue(predicate.test(result));
        }
    }

    static Stream<Arguments> delegateCallCase() {
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };
        Runnable mocker2 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        Supplier<RFuture<String>> futureSupplier = () -> new CompletableFutureWrapper<>("mock");
        Supplier<RFuture<String>> futureSupplier1 = () -> new CompletableFutureWrapper<>(new NullPointerException());
        Predicate<RFuture> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(mocker1, futureSupplier, predicate2),
                arguments(mocker2, futureSupplier, predicate2),
                arguments(mocker2, futureSupplier1, predicate2)
        );
    }
}
