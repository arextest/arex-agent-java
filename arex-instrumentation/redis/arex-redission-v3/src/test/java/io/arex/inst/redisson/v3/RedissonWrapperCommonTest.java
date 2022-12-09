package io.arex.inst.redisson.v3;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisExtractor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RFuture;
import org.redisson.misc.CompletableFutureWrapper;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class RedissonWrapperCommonTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockConstruction(RedisExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success("mock"));
        });
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("delegateCallCase")
    void delegateCall(Runnable mocker, Callable<RFuture<String>> futureCallable, Predicate<RFuture> predicate) {
        mocker.run();
        RFuture result = RedissonWrapperCommon.delegateCall("", "", "", futureCallable);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> delegateCallCase() {
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };
        Runnable mocker2 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        Callable<RFuture<String>> futureCallable = () -> new CompletableFutureWrapper<>("mock");
        Callable<RFuture<String>> futureCallable1 = () -> new CompletableFutureWrapper<>(new NullPointerException());
        Callable futureCallable2 = NullPointerException::new;
        Predicate<RFuture> predicate1 = Objects::isNull;
        Predicate<RFuture> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(mocker1, futureCallable, predicate2),
                arguments(mocker2, futureCallable, predicate2),
                arguments(mocker2, futureCallable1, predicate2),
                arguments(mocker2, futureCallable2, predicate1)
        );
    }
}