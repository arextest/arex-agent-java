package io.arex.inst.common.util;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import reactor.core.publisher.Mono;

public class MonoRecordFunction<T> implements UnaryOperator<Mono<T>> {

    private final Consumer<Object> consumer;
    private final TraceTransmitter traceTransmitter;

    public MonoRecordFunction(Consumer<Object> consumer) {
        this.traceTransmitter = TraceTransmitter.create();
        this.consumer = consumer;
    }

    @Override
    public Mono<T> apply(Mono<T> responseMono) {
        return responseMono.doOnSuccess(result -> {
            try (TraceTransmitter tm = traceTransmitter.transmit()) {
                consumer.accept(result);
            } catch (Exception ignore) {
                // ignore
            }
        }).doOnError(error -> {
            try (TraceTransmitter tm = traceTransmitter.transmit()) {
                consumer.accept(error);
            } catch (Exception ignore) {
                // ignore
            }
        });
    }
}
