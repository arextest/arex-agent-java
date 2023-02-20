package io.arex.inst.httpclient.webclient.v5.model;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.AbstractClientHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Function;

public class WebClientRequest extends AbstractClientHttpRequest {
    private ClientRequest httpRequest;
    private byte[] content = new byte[]{};
    private WebClientRequest(ClientRequest httpRequest) {
        this.httpRequest = httpRequest;
    }
    public static WebClientRequest of(ClientRequest httpRequest) {
        return new WebClientRequest(httpRequest);
    }

    @Override
    public HttpMethod getMethod() {
        return httpRequest.method();
    }

    @Override
    public URI getURI() {
        return httpRequest.url();
    }

    @Override
    public MultiValueMap<String, HttpCookie> getCookies() {
        MultiValueMap<String, HttpCookie> requestCookies = new LinkedMultiValueMap<>();
        if (!httpRequest.cookies().isEmpty()) {
            httpRequest.cookies().forEach((name, values) -> values.forEach(value -> {
                HttpCookie cookie = new HttpCookie(name, value);
                requestCookies.add(name, cookie);
            }));
        }
        return requestCookies;
    }

    @Override
    public <T> T getNativeRequest() {
        return null;
    }

    @Override
    public DataBufferFactory bufferFactory() {
        return DefaultDataBufferFactory.sharedInstance;
    }

    @Override
    protected void applyHeaders() {}

    @Override
    protected void applyCookies() {}

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return Mono.from(body).doOnSuccess(dataBuffer -> {
            if (dataBuffer != null) {
                content = dataBuffer.asByteBuffer().array();
            }
        }).then();
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return writeWith(Flux.from(body).flatMap(Function.identity()));
    }

    @Override
    public Mono<Void> setComplete() {
        return doCommit();
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.writableHttpHeaders(httpRequest.headers());
    }
    public byte[] getRequestBytes() {
        return content;
    }
}
