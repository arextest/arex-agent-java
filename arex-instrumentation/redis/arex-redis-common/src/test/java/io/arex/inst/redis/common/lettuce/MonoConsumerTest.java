package io.arex.inst.redis.common.lettuce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import io.arex.inst.redis.common.RedisExtractor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

public class MonoConsumerTest {

    static RedisExtractor extractor;

    @BeforeAll
    static void setUp() {
        extractor = Mockito.mock(RedisExtractor.class);
    }

    @AfterAll
    static void tearDown() {
        extractor = null;
        Mockito.clearAllCaches();
    }

    @Test
    void monoConsumer() {
        Mockito.doNothing().when(extractor).record(any());
        Mono<String> mono = Mono.just("test");
        Mono<String> monoResult = (Mono<String>) new MonoConsumer(extractor).accept(mono);
        assertEquals("test", monoResult.block());

        Mono<Exception> mono2 = Mono.error(new Exception("exception"));
        Mono<String> monoResult2 = (Mono<String>) new MonoConsumer(extractor).accept(mono2);
        assertThrows(Exception.class, () -> monoResult2.block());
    }
}
