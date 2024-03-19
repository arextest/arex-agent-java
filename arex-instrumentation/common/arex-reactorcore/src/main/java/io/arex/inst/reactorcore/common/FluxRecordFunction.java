package io.arex.inst.reactorcore.common;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.reactorcore.common.FluxReplayUtil.FluxResult;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.TypeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;
import reactor.core.publisher.Flux;
import java.util.function.Function;


public class FluxRecordFunction implements UnaryOperator<Flux<?>> {

    private final Function<FluxResult, Void> executor;
    private final TraceTransmitter traceTransmitter;

    public FluxRecordFunction(Function<FluxResult, Void> executor) {
        this.traceTransmitter = TraceTransmitter.create();
        this.executor = executor;
    }

    @Override
    public Flux<?> apply(Flux<?> responseFlux) {
        // use a list to record all elements
        List<FluxReplayUtil.FluxElementResult> fluxElementMockerResults = new ArrayList<>();
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
            .doOnError(error -> {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    fluxElementMockerResults.add(
                        getFluxElementMockerResult(index.getAndIncrement(), error));
                }
            })
            .doFinally(result -> {
                try (TraceTransmitter tm = traceTransmitter.transmit()) {
                    FluxResult fluxResult = new FluxResult(responseType, fluxElementMockerResults);
                    executor.apply(fluxResult);
                }
            });
    }


    private FluxReplayUtil.FluxElementResult getFluxElementMockerResult(int index, Object element) {
        String content = Serializer.serialize(element, ArexConstants.GSON_SERIALIZER);
        return new FluxReplayUtil.FluxElementResult(index, content, TypeUtil.getName(element));
    }
}
