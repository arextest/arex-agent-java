package io.arex.inst.httpclient.webclient.v5;

import io.arex.inst.httpclient.webclient.v5.model.WebClientDefaultResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientResponseBuilder implements ClientResponse.Builder {
    private static final HttpRequest EMPTY_REQUEST = new HttpRequest() {

        private final URI empty = URI.create("");

        @Override
        public String getMethodValue() {
            return "UNKNOWN";
        }

        @Override
        public URI getURI() {
            return this.empty;
        }

        @Override
        public HttpHeaders getHeaders() {
            return HttpHeaders.EMPTY;
        }
    };


    private ExchangeStrategies strategies;

    private int statusCode = 200;

    @Nullable
    private HttpHeaders headers;

    @Nullable
    private MultiValueMap<String, ResponseCookie> cookies;

    private Flux<DataBuffer> body = Flux.empty();

    @Nullable
    private ClientResponse originalResponse;

    private HttpRequest request;


    ClientResponseBuilder(ExchangeStrategies strategies) {
        this.strategies = strategies;
        this.headers = new HttpHeaders();
        this.cookies = new LinkedMultiValueMap<>();
        this.request = EMPTY_REQUEST;
    }


    @Override
    public ClientResponseBuilder statusCode(HttpStatus statusCode) {
        return rawStatusCode(statusCode.value());
    }

    @Override
    public ClientResponseBuilder rawStatusCode(int statusCode) {
        Assert.isTrue(statusCode >= 100 && statusCode < 600, "StatusCode must be between 1xx and 5xx");
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public ClientResponse.Builder header(String headerName, String... headerValues) {
        for (String headerValue : headerValues) {
            getHeaders().add(headerName, headerValue);
        }
        return this;
    }

    @Override
    public ClientResponse.Builder headers(Consumer<HttpHeaders> headersConsumer) {
        headersConsumer.accept(getHeaders());
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    private HttpHeaders getHeaders() {
        if (this.headers == null) {
            this.headers = HttpHeaders.writableHttpHeaders(this.originalResponse.headers().asHttpHeaders());
        }
        return this.headers;
    }

    @Override
    public ClientResponseBuilder cookie(String name, String... values) {
        for (String value : values) {
            getCookies().add(name, ResponseCookie.from(name, value).build());
        }
        return this;
    }

    @Override
    public ClientResponse.Builder cookies(Consumer<MultiValueMap<String, ResponseCookie>> cookiesConsumer) {
        cookiesConsumer.accept(getCookies());
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    private MultiValueMap<String, ResponseCookie> getCookies() {
        if (this.cookies == null) {
            this.cookies = new LinkedMultiValueMap<>(this.originalResponse.cookies());
        }
        return this.cookies;
    }

    @Override
    public ClientResponse.Builder body(Function<Flux<DataBuffer>, Flux<DataBuffer>> transformer) {
        this.body = transformer.apply(this.body);
        return this;
    }

    @Override
    public ClientResponse.Builder body(Flux<DataBuffer> body) {
        Assert.notNull(body, "Body must not be null");
        releaseBody();
        this.body = body;
        return this;
    }

    @Override
    public ClientResponse.Builder body(String body) {
        Assert.notNull(body, "Body must not be null");
        releaseBody();
        this.body = Flux.just(body).
                map(s -> {
                    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                    return DefaultDataBufferFactory.sharedInstance.wrap(bytes);
                });
        return this;
    }

    private void releaseBody() {
        this.body.subscribe(DataBufferUtils.releaseConsumer());
    }

    @Override
    public ClientResponse.Builder request(HttpRequest request) {
        Assert.notNull(request, "Request must not be null");
        this.request = request;
        return this;
    }

    @Override
    public ClientResponse build() {

        ClientHttpResponse httpResponse = new ClientResponseBuilder.BuiltClientHttpResponse(
                this.statusCode, this.headers, this.cookies, this.body, this.originalResponse);

        return new WebClientDefaultResponse(httpResponse, this.strategies,
                this.originalResponse != null ? this.originalResponse.logPrefix() : "",
                this.request.getMethodValue() + " " + this.request.getURI(),
                () -> this.request);
    }


    private static class BuiltClientHttpResponse implements ClientHttpResponse {

        private final int statusCode;

        @Nullable
        private final HttpHeaders headers;

        @Nullable
        private final MultiValueMap<String, ResponseCookie> cookies;

        private final Flux<DataBuffer> body;

        @Nullable
        private final ClientResponse originalResponse;


        BuiltClientHttpResponse(int statusCode, @Nullable HttpHeaders headers,
                                @Nullable MultiValueMap<String, ResponseCookie> cookies, Flux<DataBuffer> body,
                                @Nullable ClientResponse originalResponse) {

            Assert.isTrue(headers != null || originalResponse != null,
                    "Expected either headers or an original response with headers.");

            Assert.isTrue(cookies != null || originalResponse != null,
                    "Expected either cookies or an original response with cookies.");

            this.statusCode = statusCode;
            this.headers = (headers != null ? HttpHeaders.readOnlyHttpHeaders(headers) : null);
            this.cookies = (cookies != null ? CollectionUtils.unmodifiableMultiValueMap(cookies) : null);
            this.body = body;
            this.originalResponse = originalResponse;
        }

        @Override
        public HttpStatus getStatusCode() {
            return HttpStatus.valueOf(this.statusCode);
        }

        @Override
        public int getRawStatusCode() {
            return this.statusCode;
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public HttpHeaders getHeaders() {
            return (this.headers != null ? this.headers : this.originalResponse.headers().asHttpHeaders());
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public MultiValueMap<String, ResponseCookie> getCookies() {
            return (this.cookies != null ? this.cookies : this.originalResponse.cookies());
        }

        @Override
        public Flux<DataBuffer> getBody() {
            return this.body;
        }
    }
}
