package io.arex.inst.httpclient.webclient.v5.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WebClientUtilsTest {
    static ClientResponse response;

    @BeforeAll
    static void setUp() {
        response = Mockito.mock(ClientResponse.class);
        Mockito.when(response.headers()).thenReturn(Mockito.mock(ClientResponse.Headers.class));
    }

    @AfterAll
    static void tearDown() {
        response = null;
        Mockito.clearAllCaches();
    }

    @Test
    void mapToEntity() {
        assertNotNull(WebClientUtils.mapToEntity(response, Mono.empty()).subscribe());
    }

    @Test
    void mapToEntityList() {
        assertNotNull(WebClientUtils.mapToEntityList(response, Flux.just("mock")).subscribe());
    }
}