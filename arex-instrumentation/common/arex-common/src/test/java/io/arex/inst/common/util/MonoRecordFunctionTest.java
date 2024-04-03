package io.arex.inst.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class MonoRecordFunctionTest {
    @Test
    void testDoOnSuccess() {
        Object mono = Mono.just("doOnSuccess");
        List<String> list = new ArrayList<>();
        Mono<String> monoResult = new MonoRecordFunction(new Consumer<Object>() {
            @Override
            public void accept(Object object) {
                list.add((String) object);
                throw new RuntimeException("exception");
            }
        }).apply((Mono<Object>) mono);
        StepVerifier.create(monoResult).expectNext("doOnSuccess").verifyComplete();
        assertEquals("doOnSuccess", list.get(0));
    }

    @Test
    void testDoOnError() {
        Mono<?> mono = Mono.error(new RuntimeException("doOnError"));
        List<String> list = new ArrayList<>();
        Mono<String> monoResult = new MonoRecordFunction(new Consumer<Object>() {
            @Override
            public void accept(Object object) {
                list.add(((Throwable) object).getMessage());
                throw new RuntimeException("exception");
            }
        }).apply(mono);
        StepVerifier.create(monoResult).expectError().verify();
        assertEquals("doOnError", list.get(0));
    }

}
