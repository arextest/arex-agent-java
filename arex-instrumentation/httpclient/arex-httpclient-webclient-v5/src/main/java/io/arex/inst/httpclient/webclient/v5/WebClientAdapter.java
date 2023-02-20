package io.arex.inst.httpclient.webclient.v5;

import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.httpclient.common.HttpResponseWrapper.StringTuple;
import io.arex.inst.httpclient.webclient.v5.model.WebClientDefaultResponse;
import io.arex.inst.httpclient.webclient.v5.model.WebClientHttpResponse;
import io.arex.inst.httpclient.webclient.v5.model.WebClientRequest;
import io.arex.inst.httpclient.webclient.v5.model.WebClientResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WebClientAdapter implements HttpClientAdapter<ClientRequest, WebClientResponse> {
    private WebClientRequest request;
    private ClientRequest clientRequest;
    private ExchangeStrategies strategies;
    public WebClientAdapter(ClientRequest clientRequest, ExchangeStrategies strategies) {
        this.clientRequest = clientRequest;
        this.strategies = strategies;
    }

    public void setHttpRequest(WebClientRequest request) {
        this.request = request;
    }

    @Override
    public String getMethod() {
        return request.getMethod().name();
    }

    @Override
    public String getRequestContentType() {
        return request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
    }

    @Override
    public String getRequestHeader(String name) {
        return request.getHeaders().getFirst(name);
    }

    @Override
    public URI getUri() {
        return this.clientRequest.url();
    }

    @Override
    public HttpResponseWrapper wrap(WebClientResponse webClientResponse) {
        HttpResponseWrapper wrapper = new HttpResponseWrapper();
        wrapper.setContent(webClientResponse.getContent());
        ClientResponse response = webClientResponse.originalResponse();
        wrapper.setStatusLine(String.valueOf(response.statusCode().value()));
        HttpHeaders headers = response.headers().asHttpHeaders();
        wrapper.setHeaders(encodeHeaders(headers));
        return wrapper;
    }

    private List<StringTuple> encodeHeaders(HttpHeaders headers) {
        if (headers == null || headers.size() == 0) {
            return Collections.emptyList();
        }
        List<StringTuple> encodeHeaders = new ArrayList<>(headers.size());
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            encodeHeaders.add(new StringTuple(header.getKey(), String.join(",", header.getValue())));
        }
        return encodeHeaders;
    }

    @Override
    public WebClientResponse unwrap(HttpResponseWrapper wrapped) {
        WebClientHttpResponse httpResponse = WebClientHttpResponse.of(wrapped);
        String logPrefix = getLogPrefix(clientRequest, httpResponse);
        String requestDescription = clientRequest.method().name() + " " + clientRequest.url();
        WebClientDefaultResponse response = WebClientDefaultResponse.of(
                httpResponse, this.strategies, logPrefix, requestDescription, () -> createRequest(clientRequest));
        return WebClientResponse.of(response);
    }
    private String getLogPrefix(ClientRequest request, ClientHttpResponse response) {
        return request.logPrefix() + "[" + response.getId() + "] ";
    }
    private HttpRequest createRequest(ClientRequest request) {
        return new HttpRequest() {

            @Override
            public HttpMethod getMethod() {
                return request.method();
            }

            @Override
            public String getMethodValue() {
                return request.method().name();
            }

            @Override
            public URI getURI() {
                return request.url();
            }

            @Override
            public HttpHeaders getHeaders() {
                return request.headers();
            }
        };
    }
    @Override
    public byte[] getRequestBytes() {
        return request.getRequestBytes();
    }
}