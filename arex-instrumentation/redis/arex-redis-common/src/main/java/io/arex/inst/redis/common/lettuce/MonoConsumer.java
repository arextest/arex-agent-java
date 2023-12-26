package io.arex.inst.redis.common.lettuce;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.redis.common.RedisExtractor;
import reactor.core.publisher.Mono;

public class MonoConsumer {

    private final TraceTransmitter traceTransmitter;
    private final RedisExtractor extractor;

    public MonoConsumer(RedisExtractor extractor) {
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
                    extractor.record(result);
                }
            })
            .doOnError(error-> {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    extractor.record(error);
                }
            });
    }
}
