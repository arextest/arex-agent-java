package io.arex.inst.common.util;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.common.util.FluxReplayUtil.FluxResult;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.TypeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import reactor.core.publisher.Flux;


public class FluxRecordFunction<T> implements UnaryOperator<Flux<T>> {

    private final Consumer<Object> consumer;
    private final TraceTransmitter traceTransmitter;

    public FluxRecordFunction(Consumer<Object> consumer) {
        this.traceTransmitter = TraceTransmitter.create();
        this.consumer = consumer;
    }

    @Override
    public Flux<T> apply(Flux<T> flux) {
        // use a list to record all elements
        List<FluxReplayUtil.FluxElementResult> results = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(1);
        String responseType = TypeUtil.getName(flux);
        return flux.doOnNext(element -> {
            results.add(buildElementResult(index.getAndIncrement(), element));
        }).doOnError(error -> {
            results.add(buildElementResult(index.getAndIncrement(), error));
        }).doFinally(result -> {
            try (TraceTransmitter tm = traceTransmitter.transmit()) {
                consumer.accept(new FluxResult(responseType, results));
            } catch (Exception ignore) {
                // ignore
            }
        });
    }


    private FluxReplayUtil.FluxElementResult buildElementResult(int index, Object element) {
        String content = Serializer.serialize(element, ArexConstants.GSON_SERIALIZER);
        return new FluxReplayUtil.FluxElementResult(index, content, TypeUtil.getName(element));
    }
}
