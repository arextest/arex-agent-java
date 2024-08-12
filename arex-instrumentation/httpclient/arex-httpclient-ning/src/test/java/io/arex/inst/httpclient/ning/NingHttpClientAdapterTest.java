package io.arex.inst.httpclient.ning;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.uri.Uri;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.runtime.serializer.Serializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NingHttpClientAdapterTest {
    private static Request request = null;

    @BeforeAll
    static void setUp() {
        request = mock(Request.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getBodyEncoding()).thenReturn("application/json");
        when(request.getHeaders()).thenReturn(new FluentCaseInsensitiveStringsMap().add("Content-Type", "application/json"));
        mockStatic(Serializer.class);
    }

    @AfterAll
    static void tearDown() {
        request = null;
        Mockito.clearAllCaches();
    }


    @Test
    void getMethod() {
        NingHttpClientAdapter ningHttpClientAdapter = new NingHttpClientAdapter(request);
        assertEquals("GET", ningHttpClientAdapter.getMethod());
    }

    @Test
    void getRequestBytes() {
        // byteData != null
        when(request.getByteData()).thenReturn("test".getBytes());
        NingHttpClientAdapter ningHttpClientAdapter = new NingHttpClientAdapter(request);
        assertArrayEquals("test".getBytes(), ningHttpClientAdapter.getRequestBytes());

        // stringData != null
        String json = "{\"accountId\":\"uat-account-866e34f7\"}";
        when(request.getByteData()).thenReturn(null);
        when(request.getStringData()).thenReturn(json);
        assertArrayEquals(json.getBytes(StandardCharsets.UTF_8), ningHttpClientAdapter.getRequestBytes());

        // compositeByteData != null
        when(request.getByteData()).thenReturn(null);
        when(request.getStringData()).thenReturn(null);
        when(request.getCompositeByteData()).thenReturn(Arrays.asList("test1".getBytes(), "test2".getBytes()));
        assertEquals("test1test2", new String(ningHttpClientAdapter.getRequestBytes()));

        // all null
        when(request.getCompositeByteData()).thenReturn(null);
        assertArrayEquals(new byte[0], ningHttpClientAdapter.getRequestBytes());
    }

    @Test
    void getRequestContentType() {
        NingHttpClientAdapter ningHttpClientAdapter = new NingHttpClientAdapter(request);
        assertEquals("application/json", ningHttpClientAdapter.getRequestContentType());
    }

    @Test
    void getRequestHeader() {
        NingHttpClientAdapter ningHttpClientAdapter = new NingHttpClientAdapter(request);
        assertEquals("application/json", ningHttpClientAdapter.getRequestHeader("Content-Type"));
    }

    @Test
    void getUri() {
        // normal url
        when(request.getUri()).thenReturn(new Uri("http", null, "localhost", 8080, "/test", null));
        NingHttpClientAdapter ningHttpClientAdapter = new NingHttpClientAdapter(request);
        assertEquals(URI.create("http://localhost:8080/test"), ningHttpClientAdapter.getUri());

        // exception
        when(request.getUri()).thenReturn(null);
        assertDoesNotThrow(ningHttpClientAdapter::getUri);
    }

    @Test
    void wrap() throws IOException {
        Response response = mock(Response.class);
        when(response.getStatusText()).thenReturn("OK");
        when(response.getResponseBody()).thenReturn("test");
        when(response.getStatusCode()).thenReturn(200);
        FluentCaseInsensitiveStringsMap map = new FluentCaseInsensitiveStringsMap();
        map.add("key", "value1");
        map.add("key", "value2");
        map.add("Content-Type", "application/json");
        when(response.getHeaders()).thenReturn(map);
        NingHttpClientAdapter ningHttpClientAdapter = new NingHttpClientAdapter(mock(Request.class));
        HttpResponseWrapper httpResponseWrapper = ningHttpClientAdapter.wrap(response);
        assertEquals("OK", httpResponseWrapper.getStatusLine());
        assertArrayEquals("test".getBytes(), httpResponseWrapper.getContent());
        assertEquals(200, httpResponseWrapper.getStatusCode());
        for (HttpResponseWrapper.StringTuple header : httpResponseWrapper.getHeaders()) {
            if ("key".equals(header.getF())) {
                assertEquals("value1,value2", header.getS());
            }
        }
        assertNull(httpResponseWrapper.getTypeName());

        // response is not com.ning.http.client.Response
        when(Serializer.serialize(any())).thenReturn("test");
        httpResponseWrapper = ningHttpClientAdapter.wrap("test");
        assertEquals(httpResponseWrapper.getTypeName(), String.class.getName());

        // exception
        when(Serializer.serialize(any())).thenThrow(new RuntimeException());
        httpResponseWrapper = ningHttpClientAdapter.wrap(httpResponseWrapper);
        assertNotNull(httpResponseWrapper);
        assertNull(httpResponseWrapper.getContent());
    }

    @Test
    void unwrap() {
        HttpResponseWrapper httpResponseWrapper = mock(HttpResponseWrapper.class);
        NingHttpClientAdapter ningHttpClientAdapter = new NingHttpClientAdapter(mock(Request.class));
        // httpResponseWrapper responseTypeName is not empty
        when(httpResponseWrapper.getTypeName()).thenReturn(String.class.getName());
        byte[] bytes = "test".getBytes(StandardCharsets.UTF_8);
        when(httpResponseWrapper.getContent()).thenReturn(bytes);
        when(Serializer.deserialize("test", String.class.getName())).thenReturn("test");
        Object response = ningHttpClientAdapter.unwrap(httpResponseWrapper);
        assertEquals("test", response);
        // httpResponseWrapper responseTypeName is empty
        when(httpResponseWrapper.getTypeName()).thenReturn(null);
        response = ningHttpClientAdapter.unwrap(httpResponseWrapper);
        assertInstanceOf(ResponseWrapper.class, response);
    }

}
