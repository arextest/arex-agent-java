package io.arex.inst.reactorcore.common;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class MonoToFutureWrapperTest {

    @Test
    void monoToFutureTest() {
        CompletableFuture<String> future = Mono.just("test").toFuture();
        TraceTransmitter tm = TraceTransmitter.create();
        assertNotNull(MonoToFutureWrapper.thenApplyAsync(future, tm));
    }

}
