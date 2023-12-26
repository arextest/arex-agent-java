package io.arex.inst.redis.common.lettuce;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.common.util.FluxUtil;
import io.arex.inst.common.util.FluxUtil.FluxResult;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.TypeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import reactor.core.publisher.Flux;


public class FluxConsumer {

    private final RedisExtractor extractor;
    private final TraceTransmitter traceTransmitter;

    public FluxConsumer(RedisExtractor extractor) {
        this.traceTransmitter = TraceTransmitter.create();
        this.extractor = extractor;
    }

    public Flux<?> accept(Flux<?> responseFlux) {
        // use a list to record all elements
        List<FluxUtil.FluxElementResult> fluxElementMockerResults = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(1);
        String responseType = TypeUtil.getName(responseFlux);
        return responseFlux
            // add element to list
            .doOnNext(element -> {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    fluxElementMockerResults.add(
                        getFluxElementMockerResult(index.getAndIncrement(), element));
                }
            })
            // add error to list
            .doOnError(error ->  {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    fluxElementMockerResults.add(
                        getFluxElementMockerResult(index.getAndIncrement(), error));
                }
            })
            .doFinally(result -> {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    FluxResult fluxResult = new FluxResult(responseType, fluxElementMockerResults);
                    extractor.record(fluxResult);
                }
            });
    }

    private FluxUtil.FluxElementResult getFluxElementMockerResult(int index, Object element) {
        String content = Serializer.serialize(element, ArexConstants.GSON_SERIALIZER);
        return new FluxUtil.FluxElementResult(index, content, TypeUtil.getName(element));
    }
}
