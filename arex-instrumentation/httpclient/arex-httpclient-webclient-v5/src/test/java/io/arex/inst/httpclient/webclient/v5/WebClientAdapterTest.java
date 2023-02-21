package io.arex.inst.httpclient.webclient.v5;

import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.httpclient.webclient.v5.model.WebClientRequest;
import io.arex.inst.httpclient.webclient.v5.model.WebClientResponse;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.util.MockUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class WebClientAdapterTest {
    static WebClientAdapter target;
    static ClientRequest clientRequest;
    static ExchangeStrategies strategies;
    static WebClientRequest request;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(MockUtils.class);
        clientRequest = Mockito.mock(ClientRequest.class);
        strategies = Mockito.mock(ExchangeStrategies.class);
        target = new WebClientAdapter(clientRequest, strategies);
        Mockito.when(clientRequest.method()).thenReturn(HttpMethod.POST);
        Mockito.when(clientRequest.writeTo(any(), any())).thenReturn(Mono.empty());
        Mockito.when(clientRequest.url()).thenReturn(Mockito.mock(URI.class));
        Mockito.when(clientRequest.headers()).thenReturn(new HttpHeaders());
        request = WebClientRequest.of(clientRequest);
        target.setHttpRequest(request);
    }

    @AfterAll
    static void tearDown() {
        clientRequest = null;
        strategies = null;
        request = null;
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void getMethod() {
        assertNotNull(target.getMethod());
    }

    @Test
    void getRequestContentType() {
        assertNull(target.getRequestContentType());
    }

    @Test
    void getRequestHeader() {
        assertNull(target.getRequestHeader(""));
    }

    @Test
    void getUri() {
        assertNotNull(target.getUri());
    }

    @Test
    void wrap() {
        ClientResponse response = Mockito.mock(ClientResponse.class);
        Mockito.when(response.statusCode()).thenReturn(HttpStatus.OK);
        ClientResponse.Headers headers = Mockito.mock(ClientResponse.Headers.class);
        Mockito.when(response.headers()).thenReturn(headers);
        WebClientResponse webClientResponse = WebClientResponse.of(response);
        assertNotNull(target.wrap(webClientResponse));

        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.put("key", Collections.singletonList("value"));
        HttpHeaders httpHeaders = new HttpHeaders(headerMap);
        Mockito.when(headers.asHttpHeaders()).thenReturn(httpHeaders);
        assertNotNull(target.wrap(webClientResponse));
    }

    @Test
    void unwrap() {
        HttpResponseWrapper wrapper = new HttpResponseWrapper();
        wrapper.setHeaders(Collections.singletonList(new HttpResponseWrapper.StringTuple("key", "value")));
        wrapper.setContent("arex".getBytes());
        wrapper.setStatusLine("401");
        WebClientResponse clientResponse = target.unwrap(wrapper);
        clientResponse.originalResponse().createException().subscribe();
        assertNotNull(clientResponse);
    }

    @Test
    void getRequestBytes() {
        assertNotNull(target.getRequestBytes());
    }
}