package io.arex.foundation.util.httpclient;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.foundation.model.HttpClientResponse;
import io.arex.foundation.util.CompressUtil;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpClientResponseCallbackTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void test() {
        int statusCode = 200;
        String mockJsonBody = "{\"name\":\"arex\"}";
        HttpResponse mockHttpResponse = new BasicHttpResponse(
            new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), statusCode, "OK"));

        // RESPONSE Entity Exception
        CompletableFuture<HttpClientResponse> responseFuture = new CompletableFuture<>();
        new HttpClientResponseCallback(responseFuture, HttpClientResponseHandler.JsonHandler.INSTANCE).completed(
            mockHttpResponse);
        HttpClientResponse actualResponse = responseFuture.join();
        assertNull(actualResponse.getBody());

        // JSON Handler
        responseFuture = new CompletableFuture<>();
        mockHttpResponse.setEntity(new ByteArrayEntity(mockJsonBody.getBytes()));
        new HttpClientResponseCallback(responseFuture, HttpClientResponseHandler.JsonHandler.INSTANCE).completed(
            mockHttpResponse);

        actualResponse = responseFuture.join();
        assertEquals(200, actualResponse.getStatusCode());
        assertEquals(mockJsonBody, actualResponse.getBody());

        // ZSTD JSON Handler
        byte[] mockZstdJsonBody = CompressUtil.zstdCompress(mockJsonBody, StandardCharsets.UTF_8);
        mockHttpResponse.setHeader("mock-key", "mock-value");
        mockHttpResponse.setEntity(new ByteArrayEntity(mockZstdJsonBody));
        responseFuture = new CompletableFuture<>();
        new HttpClientResponseCallback(responseFuture, HttpClientResponseHandler.ZstdJsonHandler.INSTANCE).completed(
            mockHttpResponse);
        actualResponse = responseFuture.join();
        assertEquals(mockJsonBody, actualResponse.getBody());
        assertEquals(mockHttpResponse.getFirstHeader("mock-key").getValue(), actualResponse.getHeaders().get("mock-key"));

        // Failed
        responseFuture = new CompletableFuture<>();
        new HttpClientResponseCallback(responseFuture, HttpClientResponseHandler.JsonHandler.INSTANCE).failed(
            new Exception("mock exception"));
        assertTrue(responseFuture.isCompletedExceptionally());

        // Cancelled
        responseFuture = new CompletableFuture<>();
        new HttpClientResponseCallback(responseFuture, HttpClientResponseHandler.JsonHandler.INSTANCE).cancelled();
        assertTrue(responseFuture.isCompletedExceptionally());
    }
}
