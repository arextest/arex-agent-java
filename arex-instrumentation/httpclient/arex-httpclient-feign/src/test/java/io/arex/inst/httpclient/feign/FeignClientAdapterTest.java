package io.arex.inst.httpclient.feign;

import static org.junit.jupiter.api.Assertions.*;

import feign.Request;
import feign.Response;
import feign.Util;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FeignClientAdapterTest {
    private static FeignClientAdapter feignClientAdapter;

    @BeforeAll
    static void setUp() {
        final HashMap<String, Collection<String>> headers = new HashMap<>();
        headers.put("testKey", Collections.singletonList("testValue"));
        Request request = Request.create("post", "http://localhost:8080/test", headers, null, null);
        feignClientAdapter = new FeignClientAdapter(request, URI.create("http://localhost:8080/test"));
    }

    @Test
    void getMethod() {
        assertEquals("post", feignClientAdapter.getMethod());
    }

    @Test
    void getRequestBytes() {
        assertNull(feignClientAdapter.getRequestBytes());
    }

    @Test
    void getRequestContentType() {
        assertNull(feignClientAdapter.getRequestContentType());
    }

    @Test
    void getRequestHeader() {
        assertNull(feignClientAdapter.getRequestHeader("test"));
        assertEquals("testValue", feignClientAdapter.getRequestHeader("testKey"));
    }

    @Test
    void getUri() {
        assertEquals("http://localhost:8080/test", feignClientAdapter.getUri().toString());
    }

    @Test
    void wrapAndunwrap() throws IOException {
        final HashMap<String, Collection<String>> responseHeaders = new HashMap<>();
        responseHeaders.put("testKey", Collections.singletonList("testValue"));
        byte[] body = "testResponse".getBytes();
        final Response response = Response.builder().body(body).reason("test").status(200).headers(responseHeaders).build();
        final HttpResponseWrapper wrap = feignClientAdapter.wrap(response);
        assertEquals("testResponse", new String(wrap.getContent()));

        final Response unwrap = feignClientAdapter.unwrap(wrap);
        final InputStream bodyStream = unwrap.body().asInputStream();
        final byte[] bytes = Util.toByteArray(bodyStream);
        assertEquals("testResponse", new String(bytes));
    }

    @Test
    @Order(1)
    void copyResponse() {
        // null response
        assertNull(feignClientAdapter.copyResponse(null));

        byte[] body = "testResponse".getBytes();
        // repeatable response
        final Response repeatResponse = Response.builder().body(body).reason("test").status(200).headers(new HashMap<>()).build();
        final Response copyRepeatResponse = feignClientAdapter.copyResponse(repeatResponse);
        assertTrue(copyRepeatResponse.body().isRepeatable());
        assertEquals(repeatResponse.hashCode(), copyRepeatResponse.hashCode());

        // not repeatable response
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
        final Response unRepeatResponse = Response.builder().body(inputStream, 1024).reason("test").status(200).headers(new HashMap<>()).build();
        assertFalse(unRepeatResponse.body().isRepeatable());
        final Response copyUnRepeatResponse = feignClientAdapter.copyResponse(unRepeatResponse);
        assertTrue(copyUnRepeatResponse.body().isRepeatable());
        assertNotEquals(unRepeatResponse.hashCode(), copyUnRepeatResponse.hashCode());
    }
}
