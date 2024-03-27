package io.arex.inst.reactorcore.common;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import java.util.concurrent.CompletableFuture;

public class MonoToFutureWrapper {

    public static<T> CompletableFuture<T> thenApplyAsync(CompletableFuture<T> future,TraceTransmitter tm){
        return future.thenApplyAsync(content->{
            try (TraceTransmitter traceTransmitter = tm.transmit()) {
                return content;
            }
        });
    }
}
