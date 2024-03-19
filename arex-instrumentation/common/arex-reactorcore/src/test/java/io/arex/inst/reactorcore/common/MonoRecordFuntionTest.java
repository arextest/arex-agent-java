package io.arex.inst.reactorcore.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.function.Function;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

public class MonoRecordFuntionTest {

    static Function<Object, Void> executor;

    @BeforeAll
    static void setUp() {
        executor = Mockito.mock(Function.class);
    }

    @AfterAll
    static void tearDown() {
        executor = null;
        Mockito.clearAllCaches();
    }

    @Test
    void monoConsumer() {
        Mono<String> mono = Mono.just("test");
        Mono<String> monoResult = (Mono<String>) new MonoRecordFunction(executor).apply(mono);
        assertEquals("test", monoResult.block());

        Mono<Exception> mono2 = Mono.error(new Exception("exception"));
        Mono<String> monoResult2 = (Mono<String>) new MonoRecordFunction(executor).apply(mono2);
        assertThrows(Exception.class, () -> monoResult2.block());
    }

}
