package io.arex.inst.httpclient.webclient.v5;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.httpclient.webclient.v5.model.WebClientResponse;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.util.MockUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class WebClientWrapperTest {
    static ClientRequest clientRequest;
    static ExchangeStrategies strategies;
    static ClientResponse response;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(MockUtils.class);
        clientRequest = Mockito.mock(ClientRequest.class);
        strategies = Mockito.mock(ExchangeStrategies.class);
        response = Mockito.mock(ClientResponse.class);
        Mockito.when(clientRequest.method()).thenReturn(HttpMethod.POST);
        Mockito.when(clientRequest.writeTo(any(), any())).thenReturn(Mono.empty());
        Mockito.when(clientRequest.url()).thenReturn(Mockito.mock(URI.class));
        Mockito.when(clientRequest.headers()).thenReturn(new HttpHeaders());
        Mockito.mockConstruction(HttpClientExtractor.class);
    }

    @AfterAll
    static void tearDown() {
        clientRequest = null;
        strategies = null;
        response = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("recordCase")
    void record(Runnable mocker, Mono<ClientResponse> responseMono, Predicate<Mono<ClientResponse>> predicate) {
        mocker.run();
        WebClientWrapper target = new WebClientWrapper(clientRequest, strategies);
        Mono<ClientResponse> result = target.record(responseMono);
        assertTrue(predicate.test(result));
    }

    static java.util.stream.Stream<Arguments> recordCase() {
        Runnable emptyMocker = () -> {};
        DataBuffer dataBuffer = Mockito.mock(DataBuffer.class);
        ByteBuffer byteBuffer = Mockito.mock(ByteBuffer.class);
        Runnable mocker1 = () -> {
            ClientResponseBuilder builder = new ClientResponseBuilder(strategies);
            Mockito.when(byteBuffer.hasArray()).thenReturn(true);
            Mockito.when(dataBuffer.asByteBuffer()).thenReturn(byteBuffer);
            Function<Flux<DataBuffer>, Flux<DataBuffer>> transformer = dataBufferFlux -> Flux.just(dataBuffer);
            builder.body(transformer);
            Mockito.when(response.mutate()).thenReturn(builder);
        };
        Runnable mocker2 = () -> {
            Mockito.when(byteBuffer.hasArray()).thenReturn(false);
            Mockito.when(dataBuffer.toString(any())).thenReturn("mock");
        };
        Predicate<Mono<ClientResponse>> predicate1 = Objects::isNull;
        Predicate<Mono<ClientResponse>> predicate2 = responseMono -> {
            responseMono.subscribe().dispose();
            return true;
        };
        Predicate<Mono<ClientResponse>> predicate3 = responseMono -> {
            ClientResponse result = responseMono.onErrorReturn(response).block();
            return result != null;
        };
        Predicate<Mono<ClientResponse>> predicate4 = responseMono -> {
            ClientResponse result = responseMono.doOnSuccess(clientResponse -> {
                clientResponse.releaseBody().subscribe();
            }).block();
            return result != null;
        };
        return java.util.stream.Stream.of(
                arguments(emptyMocker, null, predicate1),
                arguments(emptyMocker, Mono.just(response).delaySubscription(Duration.ofSeconds(1)), predicate2),
                arguments(emptyMocker, Mono.error(new NullPointerException("mock")), predicate3),
                arguments(mocker1, Mono.just(response), predicate4),
                arguments(mocker2, Mono.just(response), predicate4)
        );
    }

    @Test
    void replay() {
        WebClientWrapper target = new WebClientWrapper(clientRequest, strategies);
        MockResult mockResult = target.replay();
        assertNull(mockResult);
    }

    @Test
    void replayTest() {
        WebClientWrapper target = new WebClientWrapper(clientRequest, strategies);
        MockResult mockResult = MockResult.success(new NullPointerException("mock"));
        Mono<ClientResponse> responseMono = target.replay(mockResult);
        assertThrows(NullPointerException.class, responseMono::block);

        MockResult mockResult1 = MockResult.success(WebClientResponse.of(response));
        assertNotNull(target.replay(mockResult1).block());
    }
}