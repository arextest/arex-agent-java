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

    /**
     * support for Mono type recording
     * @param responseMono
     * @return
     */
    public Mono<?> accept(Mono<?> responseMono) {
        return responseMono
            .doOnSuccess(result -> {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    extractor.recordResponse(result);
                }
            })
            .doOnError(error-> {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    extractor.recordResponse(error);
                }
            });
    }

}
