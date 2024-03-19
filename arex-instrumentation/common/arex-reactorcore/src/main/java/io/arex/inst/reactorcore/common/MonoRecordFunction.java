package io.arex.inst.reactorcore.common;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import reactor.core.publisher.Mono;

public class MonoRecordFunction implements UnaryOperator<Mono<?>> {

    private final Function<Object, Void> executor;
    private final TraceTransmitter traceTransmitter;

    public MonoRecordFunction(Function<Object, Void> executor) {
        this.traceTransmitter = TraceTransmitter.create();
        this.executor = executor;
    }

    @Override
    public Mono<?> apply(Mono<?> responseMono) {
        return responseMono
            .doOnSuccess(result -> {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    executor.apply(result);
                }
            })
            .doOnError(error -> {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    executor.apply(error);
                }
            });
    }
}
