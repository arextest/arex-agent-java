package io.arex.inst.httpclient.webclient.v5.model;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class WebClientRequestTest {
    static WebClientRequest target;
    static ClientRequest clientRequest;

    @BeforeAll
    static void setUp() {
        clientRequest = Mockito.mock(ClientRequest.class);
        target = WebClientRequest.of(clientRequest);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        clientRequest = null;
        Mockito.clearAllCaches();
    }

    @Test
    void getCookies() {
        MultiValueMap<String, String> cookies = new LinkedMultiValueMap<>();
        cookies.add("key", "val");
        Mockito.when(clientRequest.cookies()).thenReturn(cookies);
        assertNotNull(target.getCookies());
    }

    @Test
    void writeAndFlushWith() {
        DataBuffer dataBuffer = Mockito.mock(DataBuffer.class);
        Mockito.when(dataBuffer.toString(any())).thenReturn("mock");
        target.writeAndFlushWith(Flux.just(Flux.just(dataBuffer))).subscribe();
        assertNotNull(target.getRequestBytes());
    }
}