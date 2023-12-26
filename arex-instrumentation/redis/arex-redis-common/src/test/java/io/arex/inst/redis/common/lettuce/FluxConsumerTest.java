package io.arex.inst.redis.common.lettuce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import io.arex.inst.redis.common.RedisExtractor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

public class FluxConsumerTest {

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
    void fluxConsumer() {
        Mockito.doNothing().when(extractor).record(any());
        Flux<String> flux = Flux.just("test");
        Flux<String> fluxResult = (Flux<String>) new FluxConsumer(extractor).accept(flux);
        assertEquals("test", fluxResult.blockFirst());

        Flux<Exception> flux2 = Flux.error(new Exception("exception"));
        Flux<String> fluxResult2 = (Flux<String>) new FluxConsumer(extractor).accept(flux2);
        assertThrows(Exception.class, () -> fluxResult2.blockFirst());
    }
}
