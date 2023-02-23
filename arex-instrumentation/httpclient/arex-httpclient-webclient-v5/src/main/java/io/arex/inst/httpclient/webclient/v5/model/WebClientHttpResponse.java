package io.arex.inst.httpclient.webclient.v5.model;

import io.arex.inst.httpclient.common.HttpResponseWrapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.List;

public class WebClientHttpResponse implements ClientHttpResponse {
    private HttpResponseWrapper wrapper;

    public WebClientHttpResponse(HttpResponseWrapper wrapper) {
        this.wrapper = wrapper;
    }
    public static WebClientHttpResponse of(HttpResponseWrapper wrapper) {
        return new WebClientHttpResponse(wrapper);
    }
    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.resolve(Integer.parseInt(wrapper.getStatusLine()));
    }

    @Override
    public int getRawStatusCode() {
        return Integer.parseInt(wrapper.getStatusLine());
    }

    @Override
    public MultiValueMap<String, ResponseCookie> getCookies() {
        return new LinkedMultiValueMap<>();
    }

    @Override
    public Flux<DataBuffer> getBody() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(wrapper.getContent());
        DataBuffer buffer = bufferFactory().wrap(byteBuffer);
        return Flux.just(buffer);
    }

    @Override
    public HttpHeaders getHeaders() {
        List<HttpResponseWrapper.StringTuple> headers = wrapper.getHeaders();
        MultiValueMap<String, String> httpHeaders = new LinkedMultiValueMap<>();
        for (HttpResponseWrapper.StringTuple header : headers) {
            httpHeaders.add(header.name(), header.value());
        }
        return new HttpHeaders(httpHeaders);
    }

    public DataBufferFactory bufferFactory() {
        return DefaultDataBufferFactory.sharedInstance;
    }
}
