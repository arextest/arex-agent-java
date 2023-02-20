package io.arex.inst.httpclient.webclient.v5.util;

import org.reactivestreams.Publisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class WebClientUtils {
    private static final String VALUE_NONE = "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n";

    /**
     * Map the given response to a single value {@code ResponseEntity<T>}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Mono<ResponseEntity<T>> mapToEntity(ClientResponse response, Mono<T> bodyMono) {
        return ((Mono<Object>) bodyMono).defaultIfEmpty(VALUE_NONE).map(body ->
                new ResponseEntity<>(
                        body != VALUE_NONE ? (T) body : null,
                        response.headers().asHttpHeaders(),
                        response.rawStatusCode()));
    }

    /**
     * Map the given response to a {@code ResponseEntity<List<T>>}.
     */
    public static <T> Mono<ResponseEntity<List<T>>> mapToEntityList(ClientResponse response, Publisher<T> body) {
        return Flux.from(body).collectList().map(list ->
                new ResponseEntity<>(list, response.headers().asHttpHeaders(), response.rawStatusCode()));
    }
}
