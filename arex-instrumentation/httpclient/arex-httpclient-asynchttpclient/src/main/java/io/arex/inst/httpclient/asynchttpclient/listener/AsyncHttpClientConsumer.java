package io.arex.inst.httpclient.asynchttpclient.listener;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.httpclient.asynchttpclient.AsyncHttpClientExtractor;
import java.util.function.BiConsumer;

public class AsyncHttpClientConsumer implements BiConsumer<Object, Throwable> {
    private final AsyncHttpClientExtractor extractor;
    private final TraceTransmitter traceTransmitter;

    public AsyncHttpClientConsumer(AsyncHttpClientExtractor extractor) {
        this.traceTransmitter = TraceTransmitter.create();
        this.extractor = extractor;
    }


    @Override
    public void accept(Object o, Throwable throwable) {
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            if (throwable != null) {
                extractor.record(throwable);
            } else {
                extractor.record();
            }
        }
    }
}
