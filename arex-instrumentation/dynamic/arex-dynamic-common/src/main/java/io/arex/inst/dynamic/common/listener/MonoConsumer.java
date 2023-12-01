package io.arex.inst.dynamic.common.listener;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import reactor.core.publisher.Mono;

public class MonoConsumer {

    private final TraceTransmitter traceTransmitter;
    private final DynamicClassExtractor extractor;

    public MonoConsumer(DynamicClassExtractor extractor) {
        this.traceTransmitter = TraceTransmitter.create();
        this.extractor = extractor;
    }

    public Mono<?> accept(Mono<?> responseMono) {
        return responseMono
            .doOnSuccess(o -> {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    extractor.recordResponse(o);
                }
            })
            .doOnError(o -> {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    extractor.recordResponse(o);
                }
            });
    }
}
