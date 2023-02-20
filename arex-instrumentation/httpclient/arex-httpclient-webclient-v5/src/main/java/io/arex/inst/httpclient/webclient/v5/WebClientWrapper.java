package io.arex.inst.httpclient.webclient.v5;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.httpclient.webclient.v5.model.WebClientRequest;
import io.arex.inst.httpclient.webclient.v5.model.WebClientResponse;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WebClientWrapper {
    private final ClientRequest httpRequest;
    private final ExchangeStrategies strategies;
    private final HttpClientExtractor<ClientRequest, WebClientResponse> extractor;
    private final WebClientAdapter adapter;
    private final TraceTransmitter traceTransmitter;
    private final TraceTransmitter traceTransmitter1;
    private final TraceTransmitter traceTransmitter2;
    public WebClientWrapper(ClientRequest httpRequest, ExchangeStrategies strategies) {
        this.httpRequest = httpRequest;
        this.strategies = strategies;
        this.adapter = new WebClientAdapter(httpRequest, strategies);
        this.extractor = new HttpClientExtractor<>(this.adapter);
        this.traceTransmitter = TraceTransmitter.create();
        this.traceTransmitter1 = TraceTransmitter.create();
        this.traceTransmitter2 = TraceTransmitter.create();
    }

    public Mono<ClientResponse> record(Mono<ClientResponse> responseMono) {
        if (responseMono == null) {
            return null;
        }

        convertRequest();
        return responseMono.doOnCancel(() -> {
            try (TraceTransmitter tm = traceTransmitter.transmit()) {
                extractor.record((Exception)null);
            }
        }).doOnError(throwable -> {
            try (TraceTransmitter tm = traceTransmitter.transmit()) {
                extractor.record(throwable);
            }
        }).map(response -> {
            try (TraceTransmitter tm = traceTransmitter.transmit()) {
                List<byte[]> bytes = new ArrayList<>();
                ClientResponse clientResponse = response.mutate().body(body -> {
                    return Flux.defer(() -> Flux.from(body)
                            .map(dataBuffer -> {
                                if (dataBuffer != null) {
                                    ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                                    /*
                                     * If the response message is too large, it will be truncated and returned multiple times.
                                     * Here, aggregation is required
                                     */
                                    if (byteBuffer.hasArray()) {
                                        bytes.add(byteBuffer.array());
                                    } else {
                                        bytes.add(dataBuffer.toString(StandardCharsets.UTF_8).getBytes());
                                    }
                                }
                                return dataBuffer;
                            }).doOnComplete(() -> {
                                byte[] bodyArray = new byte[]{};
                                for (byte[] byteArray : bytes) {
                                    bodyArray = ArrayUtils.addAll(bodyArray, byteArray);
                                }
                                try (TraceTransmitter tm1 = traceTransmitter1.transmit()) {
                                    extractor.record(WebClientResponse.of(response, bodyArray));
                                }
                            })
                    );
                }).build();
                return clientResponse;
            }
        });
    }

    private void convertRequest() {
        WebClientRequest request = WebClientRequest.of(httpRequest);
        Mono<Void> requestMono = httpRequest.writeTo(request, strategies);
        if (requestMono != null) {
            requestMono.subscribe();
            adapter.setHttpRequest(request);
        }
    }

    public MockResult replay() {
        try (TraceTransmitter tm = traceTransmitter2.transmit()) {
            convertRequest();
            return extractor.replay();
        }
    }

    public Mono<ClientResponse> replay(MockResult mockResult) {
        if (mockResult.getThrowable() != null) {
            return Mono.error(mockResult.getThrowable());
        }
        WebClientResponse webClientResponse = (WebClientResponse)mockResult.getResult();
        return Mono.just(webClientResponse.originalResponse());
    }
}